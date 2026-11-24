package com.mygolfscore.app.data.drive

import android.accounts.Account
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.edit
import com.google.android.gms.auth.GoogleAuthUtil
import com.mygolfscore.app.data.model.DriveFolderConfig
import com.mygolfscore.app.data.model.GolfScorePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class GoogleDriveRepository(private val context: Context) {

    companion object {
        const val DRIVE_SCOPE = "oauth2:https://www.googleapis.com/auth/drive"
        val DEFAULT_CONFIG = DriveFolderConfig()
        private const val CACHE_FILE_NAME = "cached_myGolfScore.json"
        private const val PREF_NAME = "google_drive_cache_prefs"
        private const val KEY_FILE_ID = "cached_file_id"
        private const val KEY_MODIFIED_TIME = "cached_modified_time"
    }

    private val jsonParser = Json { ignoreUnknownKeys = true }
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun loadLocalSample(): GolfScorePayload {
        val raw = context.assets.open("myGolfScore.json").bufferedReader().use { it.readText() }
        return jsonParser.decodeFromString<GolfScorePayload>(raw)
    }

    fun loadInitialPayload(): Pair<GolfScorePayload, Boolean> {
        val cacheFile = File(context.filesDir, CACHE_FILE_NAME)
        if (cacheFile.exists() && (cacheFile.length() > 0)) {
            val cachedPayload = runCatching {
                val raw = cacheFile.readText()
                jsonParser.decodeFromString<GolfScorePayload>(raw)
            }.getOrNull()

            cachedPayload?.let {
                return Pair(it, false)
            }
        }
        return Pair(loadLocalSample(), true)
    }

    suspend fun fetchAccessToken(account: Account): String = withContext(Dispatchers.IO) {
        GoogleAuthUtil.getToken(context, account, DRIVE_SCOPE)
    }

    suspend fun downloadScoreJson(
        accessToken: String,
        folderId: String = DEFAULT_CONFIG.rootFolderId,
        fileName: String = DEFAULT_CONFIG.jsonFileName,
    ): Result<GolfScorePayload> = withContext(Dispatchers.IO) {
        runCatching {
            val query = "'$folderId' in parents and name='$fileName' and trashed=false"
            val searchUrl = "https://www.googleapis.com/drive/v3/files" +
                "?q=${Uri.encode(query)}&orderBy=modifiedTime%20desc&pageSize=1&fields=files(id,name,modifiedTime)"

            val searchReq = Request.Builder()
                .url(searchUrl)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            val (fileId, modifiedTime) = httpClient.newCall(searchReq).execute().use { resp ->
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

            val cacheFile = File(context.filesDir, CACHE_FILE_NAME)
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val savedFileId = prefs.getString(KEY_FILE_ID, null)
            val savedModifiedTime = prefs.getString(KEY_MODIFIED_TIME, null)

            val isCacheValid = cacheFile.exists() && (cacheFile.length() > 0) &&
                (savedFileId == fileId) && (savedModifiedTime == modifiedTime) && modifiedTime.isNotEmpty()
            if (isCacheValid) {
                val cachedPayload = runCatching {
                    val jsonText = cacheFile.readText()
                    jsonParser.decodeFromString<GolfScorePayload>(jsonText)
                }.getOrNull()

                cachedPayload?.let {
                    return@runCatching it
                }
            }

            val downloadUrl = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
            val dlReq = Request.Builder()
                .url(downloadUrl)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            httpClient.newCall(dlReq).execute().use { resp ->
                if (!resp.isSuccessful) {
                    error("파일 다운로드 실패 (${resp.code}): ${resp.body?.string().orEmpty()}")
                }
                val jsonText = resp.body?.string().orEmpty()
                val payload = jsonParser.decodeFromString<GolfScorePayload>(jsonText)

                // Atomic cache write via temp file
                val tempFile = File(context.filesDir, "$CACHE_FILE_NAME.tmp")
                runCatching {
                    tempFile.writeText(jsonText)
                    if (tempFile.exists() && (tempFile.length() > 0)) {
                        tempFile.renameTo(cacheFile)
                    }
                }

                prefs.edit {
                    putString(KEY_FILE_ID, fileId)
                    putString(KEY_MODIFIED_TIME, modifiedTime)
                }

                payload
            }
        }
    }

    suspend fun uploadScorecardImage(
        accessToken: String,
        imageUri: Uri,
        imageFolderId: String = DEFAULT_CONFIG.imageFolderId,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
            val fileName = resolveFileName(imageUri) ?: "scorecard_${System.currentTimeMillis()}.jpg"
            val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                ?: error("선택한 이미지를 읽을 수 없습니다.")

            val metadataJson = JSONObject().apply {
                put("name", fileName)
                put("parents", JSONArray().put(imageFolderId))
            }.toString()

            val multipartBody = MultipartBody.Builder()
                .setType("multipart/related".toMediaType())
                .addPart(metadataJson.toRequestBody("application/json; charset=UTF-8".toMediaType()))
                .addPart(bytes.toRequestBody(mimeType.toMediaType()))
                .build()

            val req = Request.Builder()
                .url("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id,name")
                .addHeader("Authorization", "Bearer $accessToken")
                .post(multipartBody)
                .build()

            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    error("이미지 업로드 실패 (${resp.code}): ${resp.body?.string().orEmpty()}")
                }
                val respJson = JSONObject(resp.body?.string().orEmpty())
                respJson.optString("name", fileName)
            }
        }
    }

    private fun resolveFileName(uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if ((idx >= 0) && cursor.moveToFirst()) {
                    cursor.getString(idx)
                } else {
                    null
                }
            }
        }.getOrNull()
    }
}

