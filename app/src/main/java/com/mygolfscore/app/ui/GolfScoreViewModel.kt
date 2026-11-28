package com.mygolfscore.app.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.mygolfscore.app.data.drive.GoogleDriveRepository
import com.mygolfscore.app.data.drive.NeedAuthConsentException
import com.mygolfscore.app.data.model.GolfRound
import com.mygolfscore.app.data.model.GolfScorePayload
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GolfScoreUiState(
    val isSignedIn: Boolean = false,
    val accountEmail: String? = null,
    val accountDisplayName: String? = null,
    val accountPhotoUrl: String? = null,
    val isUsingOfflineSample: Boolean = true,
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val payload: GolfScorePayload = GolfScorePayload(),
    val selectedRound: GolfRound? = null,
    val selectedImageUri: Uri? = null,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val lastSyncedAt: String? = null,
    val isLoadingRoundImage: Boolean = false,
    val viewingImageBytes: ByteArray? = null,
    val authConsentIntent: Intent? = null,
)

class GolfScoreViewModel(application: Application) : AndroidViewModel(application) {

    private enum class ConsentPendingAction {
        SYNC,
        UPLOAD
    }

    private val repository = GoogleDriveRepository(application.applicationContext)
    private var syncJob: Job? = null
    private var uploadJob: Job? = null
    private var authCleanupJob: Job? = null
    private var pendingConsentAction: ConsentPendingAction? = null
    private var imageJob: Job? = null

    private fun currentFormattedTime(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

    private val _uiState = MutableStateFlow(GolfScoreUiState())
    val uiState: StateFlow<GolfScoreUiState> = _uiState.asStateFlow()

    init {
        val (initialPayload, isOfflineSample) = runCatching { repository.loadInitialPayload() }
            .getOrDefault(Pair(GolfScorePayload(), true))
        val lastSignedIn = GoogleSignIn.getLastSignedInAccount(application)
        _uiState.update {
            it.copy(
                payload = initialPayload,
                isUsingOfflineSample = isOfflineSample,
                isSignedIn = lastSignedIn != null,
                accountEmail = lastSignedIn?.email,
                accountDisplayName = lastSignedIn?.displayName,
                accountPhotoUrl = lastSignedIn?.photoUrl?.toString()
            )
        }
        if (lastSignedIn != null) {
            syncFromDrive(lastSignedIn)
        }
    }

    fun onGoogleSignInSuccess(account: GoogleSignInAccount) {
        pendingConsentAction = null
        _uiState.update {
            it.copy(
                isSignedIn = true,
                accountEmail = account.email,
                accountDisplayName = account.displayName,
                accountPhotoUrl = account.photoUrl?.toString(),
                errorMessage = null,
                authConsentIntent = null,
                statusMessage = "구글 계정 연결됨 (${account.email})"
            )
        }
        syncFromDrive(account)
    }

    fun onGoogleSignOutSuccess() {
        val prevSync = syncJob
        syncJob = null
        prevSync?.cancel()

        val prevUpload = uploadJob
        uploadJob = null
        prevUpload?.cancel()

        pendingConsentAction = null

        val sample = runCatching { repository.loadLocalSample() }.getOrDefault(GolfScorePayload())
        _uiState.update {
            it.copy(
                isSignedIn = false,
                accountEmail = null,
                accountDisplayName = null,
                accountPhotoUrl = null,
                isUsingOfflineSample = true,
                isLoading = false,
                isUploading = false,
                payload = sample,
                selectedRound = null,
                selectedImageUri = null,
                errorMessage = null,
                lastSyncedAt = null,
                isLoadingRoundImage = false,
                viewingImageBytes = null,
                authConsentIntent = null,
                statusMessage = "로그아웃되었습니다."
            )
        }

        val prevCleanup = authCleanupJob
        authCleanupJob = viewModelScope.launch {
            prevCleanup?.join()
            prevSync?.join()
            prevUpload?.join()
            repository.clearCache()
        }
    }

    fun onGoogleSignInFailed(message: String) {
        _uiState.update {
            it.copy(errorMessage = "로그인 실패: $message")
        }
    }

    fun refreshFromDrive() {
        if (_uiState.value.isLoading || syncJob?.isActive == true) return
        val account = GoogleSignIn.getLastSignedInAccount(getApplication())
        if (account == null) {
            _uiState.update { it.copy(errorMessage = "먼저 구글 계정으로 로그인해 주세요.") }
            return
        }
        syncFromDrive(account)
    }

    private fun syncFromDrive(account: GoogleSignInAccount) {
        val gAccount = account.account ?: return
        if (_uiState.value.isLoading || syncJob?.isActive == true) return

        lateinit var job: Job
        job = viewModelScope.launch(start = CoroutineStart.LAZY) {
            authCleanupJob?.join()
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = repository.downloadScoreJson(gAccount).getOrThrow()
                val msg = if (result.fromCache) {
                    "이미 최신 상태입니다 (${result.payload.rounds.size}경기)"
                } else {
                    "구글 드라이브 최신 데이터 동기화 완료 (${result.payload.rounds.size}경기)"
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isUsingOfflineSample = false,
                        payload = result.payload,
                        lastSyncedAt = currentFormattedTime(),
                        statusMessage = msg
                    )
                }
            } catch (ce: CancellationException) {
                if (syncJob === job) {
                    _uiState.update { it.copy(isLoading = false) }
                }
                throw ce
            } catch (consent: NeedAuthConsentException) {
                pendingConsentAction = ConsentPendingAction.SYNC
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        authConsentIntent = consent.consentIntent
                    )
                }
            } catch (err: Throwable) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.message ?: "드라이브 동기화 중 오류가 발생했습니다."
                    )
                }
            } finally {
                if (syncJob === job) {
                    syncJob = null
                }
            }
        }
        syncJob = job
        job.start()
    }

    fun selectRound(round: GolfRound?) {
        _uiState.update { it.copy(selectedRound = round) }
    }

    fun selectImage(uri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri, errorMessage = null, statusMessage = null) }
    }

    fun uploadSelectedImage() {
        if (_uiState.value.isUploading || uploadJob?.isActive == true) return
        val uri = _uiState.value.selectedImageUri ?: return
        val account = GoogleSignIn.getLastSignedInAccount(getApplication())?.account
        if (account == null) {
            _uiState.update { it.copy(errorMessage = "업로드하려면 먼저 구글 계정으로 로그인해 주세요.") }
            return
        }

        lateinit var job: Job
        job = viewModelScope.launch(start = CoroutineStart.LAZY) {
            authCleanupJob?.join()
            _uiState.update { it.copy(isUploading = true, errorMessage = null, statusMessage = null) }
            try {
                val uploadedName = repository.uploadScorecardImage(account, uri).getOrThrow()
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        selectedImageUri = null,
                        statusMessage = "GolfScore/Image 폴더에 업로드 완료: $uploadedName"
                    )
                }
            } catch (ce: CancellationException) {
                if (uploadJob === job) {
                    _uiState.update { it.copy(isUploading = false) }
                }
                throw ce
            } catch (consent: NeedAuthConsentException) {
                pendingConsentAction = ConsentPendingAction.UPLOAD
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        authConsentIntent = consent.consentIntent
                    )
                }
            } catch (err: Throwable) {
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        errorMessage = err.message ?: "업로드 중 오류가 발생했습니다."
                    )
                }
            } finally {
                if (uploadJob === job) {
                    uploadJob = null
                }
            }
        }
        uploadJob = job
        job.start()
    }

    fun consumeAuthConsentIntent() {
        _uiState.update { it.copy(authConsentIntent = null) }
    }

    fun onAuthConsentResult(granted: Boolean) {
        val action = pendingConsentAction
        pendingConsentAction = null
        if (!granted) {
            _uiState.update {
                it.copy(errorMessage = "Google Drive 권한 승인이 취소되었습니다.")
            }
            return
        }
        when (action) {
            ConsentPendingAction.SYNC -> refreshFromDrive()
            ConsentPendingAction.UPLOAD -> uploadSelectedImage()
            null -> Unit
        }
    }

    fun loadOriginalScorecardImage(round: GolfRound) {
        if (round.imageFileId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "연결된 원본 스코어카드 이미지가 없습니다.") }
            return
        }
        val account = GoogleSignIn.getLastSignedInAccount(getApplication())?.account
        if (account == null) {
            _uiState.update { it.copy(errorMessage = "이미지를 보려면 먼저 로그인해 주세요.") }
            return
        }
        imageJob?.cancel()
        imageJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRoundImage = true, errorMessage = null) }
            val res = repository.downloadImageBytes(account, round.imageFileId)
            res.onSuccess { bytes ->
                _uiState.update { it.copy(isLoadingRoundImage = false, viewingImageBytes = bytes) }
            }.onFailure { err ->
                if (err is NeedAuthConsentException) {
                    _uiState.update { it.copy(isLoadingRoundImage = false, authConsentIntent = err.consentIntent) }
                } else if (err !is CancellationException) {
                    _uiState.update { it.copy(isLoadingRoundImage = false, errorMessage = err.message ?: "이미지를 불러오지 못했습니다.") }
                }
            }
        }
    }

    fun dismissOriginalScorecardImage() {
        imageJob?.cancel()
        _uiState.update { it.copy(isLoadingRoundImage = false, viewingImageBytes = null) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }
}
