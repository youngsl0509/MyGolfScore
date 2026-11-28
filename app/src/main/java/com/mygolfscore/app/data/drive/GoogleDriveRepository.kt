package com.mygolfscore.app.data.drive

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.edit
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.mygolfscore.app.data.model.DriveFolderConfig
import com.mygolfscore.app.data.model.GolfScorePayload
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.BufferedSink
import okio.source
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

data class SyncResult(
    val payload: GolfScorePayload,
    val fromCache: Boolean
)

class NeedAuthConsentException(val consentIntent: Intent) :
    Exception("Google Drive 권한 재승인이 필요합니다. 권한을 승인해 주세요.")

class GoogleDriveRepository(private val context: Context) {

    companion object {
        const val DRIVE_SCOPE = "oauth2:https://www.googleapis.com/auth/drive"
        val DEFAULT_CONFIG = DriveFolderConfig()
        private const val CACHE_FILE_NAME = "cached_myGolfScore.json"
        private const val PREF_NAME = "google_drive_cache_prefs"
        private const val KEY_FILE_ID = "cached_file_id"
        private const val KEY_MODIFIED_TIME = "cached_modified_time"
        private const val MAX_TRANSIENT_RETRIES = 2
    }

    private sealed interface AttemptOutcome<out T> {
        data class Success<T>(val value: T) : AttemptOutcome<T>
        data class RefreshAuthAndRetry(val staleToken: String) : AttemptOutcome<Nothing>
        data object RetryWithBackoff : AttemptOutcome<Nothing>
    }

    private val cacheMutex = Mutex()
    private val prefs by lazy { context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) }
    private val jsonParser = Json { ignoreUnknownKeys = true }
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun cacheFile(): File = File(context.filesDir, CACHE_FILE_NAME)

    private fun tempCacheFile(): File = File(context.filesDir, "$CACHE_FILE_NAME.tmp")

    fun loadLocalSample(): GolfScorePayload {
        val raw = context.assets.open("myGolfScore.json").bufferedReader().use { it.readText() }
        return jsonParser.decodeFromString<GolfScorePayload>(raw)
    }

    fun loadInitialPayload(): Pair<GolfScorePayload, Boolean> {
        val file = cacheFile()
        if (file.exists() && file.length() > 0) {
            val cachedPayload = runCatching {
                jsonParser.decodeFromString<GolfScorePayload>(file.readText(Charsets.UTF_8))
            }.getOrNull()
            if (cachedPayload != null) {
                return Pair(cachedPayload, false)
            }
        }
        return Pair(loadLocalSample(), true)
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        cacheMutex.withLock {
            cacheFile().delete()
            tempCacheFile().delete()
            prefs.edit { clear() }
        }
    }

    suspend fun fetchAccessToken(account: Account): String = withContext(Dispatchers.IO) {
        try {
            GoogleAuthUtil.getToken(context, account, DRIVE_SCOPE)
        } catch (recoverable: UserRecoverableAuthException) {
            val intent = recoverable.intent
            if (intent != null) {
                throw NeedAuthConsentException(intent)
            }
            throw recoverable
        }
    }

    private suspend fun <T> executeCall(
        request: Request,
        block: (Response) -> T
    ): T {
        val call = httpClient.newCall(request)
        val handle = currentCoroutineContext()[Job]?.invokeOnCompletion { cause ->
            if (cause != null) {
                call.cancel()
            }
        }
        return try {
            call.execute().use { resp -> block(resp) }
        } finally {
            handle?.dispose()
        }
    }

    private suspend fun <T> executeWithAuthAndRetry(
        account: Account,
        buildRequest: (accessToken: String) -> Request,
        parseResponse: (Response) -> T
    ): T {
        var token: String? = null
        var clearedAuthOnce = false
        var transientAttempt = 0

        while (true) {
            currentCoroutineContext().ensureActive()
            val outcome = try {
                val activeToken = token ?: fetchAccessToken(account).also { token = it }
                val request = buildRequest(activeToken)
                executeCall(request) { resp ->
                    when {
                        resp.code == 401 && !clearedAuthOnce -> {
                            clearedAuthOnce = true
                            AttemptOutcome.RefreshAuthAndRetry(activeToken)
                        }
                        (resp.code == 429 || resp.code in 500..599) && transientAttempt < MAX_TRANSIENT_RETRIES -> {
                            AttemptOutcome.RetryWithBackoff
                        }
                        else -> AttemptOutcome.Success(parseResponse(resp))
                    }
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (ioe: IOException) {
                currentCoroutineContext().ensureActive()
                if (transientAttempt >= MAX_TRANSIENT_RETRIES) {
                    throw ioe
                }
                AttemptOutcome.RetryWithBackoff
            }

            when (outcome) {
                is AttemptOutcome.Success -> return outcome.value
                is AttemptOutcome.RefreshAuthAndRetry -> {
                    runCatching { GoogleAuthUtil.clearToken(context, outcome.staleToken) }
                    token = null
                    continue
                }
                AttemptOutcome.RetryWithBackoff -> {
                    transientAttempt++
                    delay(1000L * transientAttempt)
                }
            }
        }
    }

    suspend fun downloadScoreJson(
        account: Account,
        folderId: String = DEFAULT_CONFIG.rootFolderId,
        fileName: String = DEFAULT_CONFIG.jsonFileName,
        forceRefresh: Boolean = false
    ): Result<SyncResult> = withContext(Dispatchers.IO) {
        try {
            val escapedFolderId = folderId.replace("\\", "\\\\").replace("'", "\\'")
            val escapedFileName = fileName.replace("\\", "\\\\").replace("'", "\\'")
            val query = "'$escapedFolderId' in parents and name='$escapedFileName' and trashed=false"
            val searchUrl = "https://www.googleapis.com/drive/v3/files" +
                "?q=${Uri.encode(query)}&orderBy=modifiedTime%20desc&pageSize=1&fields=files(id,name,modifiedTime)"

            val (fileId, modifiedTime) = executeWithAuthAndRetry(
                account = account,
                buildRequest = { token ->
                    Request.Builder()
                        .url(searchUrl)
                        .addHeader("Authorization", "Bearer $token")
                        .get()
                        .build()
                },
                parseResponse = { resp ->
                    if (!resp.isSuccessful) {
                        error("Drive 검색 실패 (${resp.code}): ${resp.body?.string().orEmpty()}")
                    }
                    val bodyStr = resp.body?.string().orEmpty()
                    val files = jsonParser.parseToJsonElement(bodyStr).jsonObject["files"]?.jsonArray
                    if (files.isNullOrEmpty()) {
                        error("구글 드라이브 GolfScore 폴더에서 $fileName 파일을 찾을 수 없습니다.")
                    }
                    val fileObj = files[0].jsonObject
                    val id = fileObj["id"]?.jsonPrimitive?.content
                        ?: error("파일 ID를 읽을 수 없습니다.")
                    val modTime = fileObj["modifiedTime"]?.jsonPrimitive?.content.orEmpty()
                    id to modTime
                }
            )

            if (!forceRefresh) {
                val cachedPayload = cacheMutex.withLock {
                    val file = cacheFile()
                    val savedFileId = prefs.getString(KEY_FILE_ID, null)
                    val savedModifiedTime = prefs.getString(KEY_MODIFIED_TIME, null)
                    val isCacheValid = file.exists() && file.length() > 0 &&
                        savedFileId == fileId && savedModifiedTime == modifiedTime && modifiedTime.isNotEmpty()

                    if (!isCacheValid) {
                        null
                    } else {
                        val decoded = runCatching {
                            jsonParser.decodeFromString<GolfScorePayload>(file.readText(Charsets.UTF_8))
                        }.getOrNull()
                        if (decoded == null) {
                            file.delete()
                            prefs.edit {
                                remove(KEY_FILE_ID)
                                remove(KEY_MODIFIED_TIME)
                            }
                        }
                        decoded
                    }
                }
                if (cachedPayload != null) {
                    return@withContext Result.success(SyncResult(cachedPayload, fromCache = true))
                }
            }

            val downloadUrl = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
            val (jsonText, payload) = executeWithAuthAndRetry(
                account = account,
                buildRequest = { token ->
                    Request.Builder()
                        .url(downloadUrl)
                        .addHeader("Authorization", "Bearer $token")
                        .get()
                        .build()
                },
                parseResponse = { resp ->
                    if (!resp.isSuccessful) {
                        error("파일 다운로드 실패 (${resp.code}): ${resp.body?.string().orEmpty()}")
                    }
                    val text = resp.body?.string().orEmpty()
                    text to jsonParser.decodeFromString<GolfScorePayload>(text)
                }
            )

            currentCoroutineContext().ensureActive()

            cacheMutex.withLock {
                val file = cacheFile()
                val tempFile = tempCacheFile()
                val writeSucceeded = runCatching {
                    tempFile.writeText(jsonText, Charsets.UTF_8)
                    if (tempFile.exists() && tempFile.length() > 0) {
                        if (!tempFile.renameTo(file)) {
                            file.writeText(jsonText, Charsets.UTF_8)
                            tempFile.delete()
                        }
                    }
                    file.exists() && file.length() > 0
                }.getOrDefault(false)

                if (writeSucceeded) {
                    prefs.edit {
                        putString(KEY_FILE_ID, fileId)
                        putString(KEY_MODIFIED_TIME, modifiedTime)
                    }
                } else {
                    tempFile.delete()
                    prefs.edit {
                        remove(KEY_FILE_ID)
                        remove(KEY_MODIFIED_TIME)
                    }
                }
            }

            Result.success(SyncResult(payload, fromCache = false))
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }


    suspend fun downloadImageBytes(
        account: Account,
        fileId: String
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val downloadUrl = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
            val bytes = executeWithAuthAndRetry(
                account = account,
                buildRequest = { token ->
                    Request.Builder()
                        .url(downloadUrl)
                        .addHeader("Authorization", "Bearer $token")
                        .get()
                        .build()
                },
                parseResponse = { resp ->
                    if (!resp.isSuccessful) {
                        error("이미지 불러오기 실패 (${resp.code})")
                    }
                    resp.body?.bytes() ?: error("이미지 데이터가 비어 있습니다.")
                }
            )
            Result.success(bytes)
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    suspend fun uploadScorecardImage(
        account: Account,
        imageUri: Uri,
        imageFolderId: String = DEFAULT_CONFIG.imageFolderId
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
            val fileName = resolveFileName(imageUri, mimeType)

            val metadataJson = JSONObject().apply {
                put("name", fileName)
                put("parents", JSONArray().put(imageFolderId))
            }.toString()

            val streamingBody = object : RequestBody() {
                override fun contentType(): MediaType =
                    mimeType.toMediaTypeOrNull() ?: "image/jpeg".toMediaType()
                override fun writeTo(sink: BufferedSink) {
                    val input = context.contentResolver.openInputStream(imageUri)
                        ?: error("선택한 이미지를 읽을 수 없습니다.")
                    input.source().use { src ->
                        sink.writeAll(src)
                    }
                }
            }

            val uploadedName = executeWithAuthAndRetry(
                account = account,
                buildRequest = { token ->
                    val multipartBody = MultipartBody.Builder()
                        .setType("multipart/related".toMediaType())
                        .addPart(metadataJson.toRequestBody("application/json; charset=UTF-8".toMediaType()))
                        .addPart(streamingBody)
                        .build()

                    Request.Builder()
                        .url("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id,name")
                        .addHeader("Authorization", "Bearer $token")
                        .post(multipartBody)
                        .build()
                },
                parseResponse = { resp ->
                    if (!resp.isSuccessful) {
                        error("이미지 업로드 실패 (${resp.code}): ${resp.body?.string().orEmpty()}")
                    }
                    val respJson = JSONObject(resp.body?.string().orEmpty())
                    respJson.optString("name", fileName)
                }
            )
            Result.success(uploadedName)
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    private fun resolveFileName(uri: Uri, mimeType: String): String {
        val rawName = runCatching {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
            }
        }.getOrNull()
        val baseName = rawName?.takeIf { it.isNotBlank() } ?: "scorecard_${System.currentTimeMillis()}"
        val safeName = baseName.substringAfterLast('/').ifBlank { "scorecard_${System.currentTimeMillis()}" }
        val dotIndex = safeName.lastIndexOf('.')
        val existingExt = if (dotIndex > 0 && dotIndex < safeName.length - 1) {
            safeName.substring(dotIndex + 1).lowercase()
        } else {
            null
        }
        val mimeMap = MimeTypeMap.getSingleton()
        return if (existingExt != null && mimeMap.hasExtension(existingExt)) {
            safeName
        } else {
            val ext = mimeMap.getExtensionFromMimeType(mimeType) ?: "jpg"
            "$safeName.$ext"
        }
    }
}
