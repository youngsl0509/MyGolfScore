@file:Suppress("DEPRECATION")

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

enum class RetryAction {
    SYNC,
    UPLOAD
}

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
    val snackbarActionLabel: String? = null,
    val snackbarRetryAction: RetryAction? = null,
    val lastSyncedAt: String? = null,
    val isViewerOpen: Boolean = false,
    val isLoadingRoundImage: Boolean = false,
    val viewingImageBytes: ByteArray? = null,
    val imageErrorMessage: String? = null,
    val authConsentIntent: Intent? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as GolfScoreUiState
        if (isSignedIn != other.isSignedIn) return false
        if (accountEmail != other.accountEmail) return false
        if (accountDisplayName != other.accountDisplayName) return false
        if (accountPhotoUrl != other.accountPhotoUrl) return false
        if (isUsingOfflineSample != other.isUsingOfflineSample) return false
        if (isLoading != other.isLoading) return false
        if (isUploading != other.isUploading) return false
        if (payload != other.payload) return false
        if (selectedRound != other.selectedRound) return false
        if (selectedImageUri != other.selectedImageUri) return false
        if (statusMessage != other.statusMessage) return false
        if (errorMessage != other.errorMessage) return false
        if (snackbarActionLabel != other.snackbarActionLabel) return false
        if (snackbarRetryAction != other.snackbarRetryAction) return false
        if (lastSyncedAt != other.lastSyncedAt) return false
        if (isViewerOpen != other.isViewerOpen) return false
        if (isLoadingRoundImage != other.isLoadingRoundImage) return false
        if (!viewingImageBytes.contentEquals(other.viewingImageBytes)) return false
        if (imageErrorMessage != other.imageErrorMessage) return false
        if (authConsentIntent != other.authConsentIntent) return false
        return true
    }

    override fun hashCode(): Int {
        var result = isSignedIn.hashCode()
        result = 31 * result + (accountEmail?.hashCode() ?: 0)
        result = 31 * result + (accountDisplayName?.hashCode() ?: 0)
        result = 31 * result + (accountPhotoUrl?.hashCode() ?: 0)
        result = 31 * result + isUsingOfflineSample.hashCode()
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + isUploading.hashCode()
        result = 31 * result + payload.hashCode()
        result = 31 * result + (selectedRound?.hashCode() ?: 0)
        result = 31 * result + (selectedImageUri?.hashCode() ?: 0)
        result = 31 * result + (statusMessage?.hashCode() ?: 0)
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        result = 31 * result + (snackbarActionLabel?.hashCode() ?: 0)
        result = 31 * result + (snackbarRetryAction?.hashCode() ?: 0)
        result = 31 * result + (lastSyncedAt?.hashCode() ?: 0)
        result = 31 * result + isViewerOpen.hashCode()
        result = 31 * result + isLoadingRoundImage.hashCode()
        result = 31 * result + (viewingImageBytes?.contentHashCode() ?: 0)
        result = 31 * result + (imageErrorMessage?.hashCode() ?: 0)
        result = 31 * result + (authConsentIntent?.hashCode() ?: 0)
        return result
    }
}

class GolfScoreViewModel(application: Application) : AndroidViewModel(application) {

    private enum class ConsentPendingAction {
        SYNC,
        UPLOAD,
        VIEW_IMAGE
    }

    private val repository = GoogleDriveRepository(application.applicationContext)
    private var syncJob: Job? = null
    private var uploadJob: Job? = null
    private var authCleanupJob: Job? = null
    private var pendingConsentAction: ConsentPendingAction? = null
    private var pendingImageRound: GolfRound? = null
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
        pendingImageRound = null
        _uiState.update {
            it.copy(
                isSignedIn = true,
                accountEmail = account.email,
                accountDisplayName = account.displayName,
                accountPhotoUrl = account.photoUrl?.toString(),
                errorMessage = null,
                snackbarActionLabel = null,
                snackbarRetryAction = null,
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

        val prevImage = imageJob
        imageJob = null
        prevImage?.cancel()

        pendingConsentAction = null
        pendingImageRound = null

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
                snackbarActionLabel = null,
                snackbarRetryAction = null,
                lastSyncedAt = null,
                isViewerOpen = false,
                isLoadingRoundImage = false,
                viewingImageBytes = null,
                imageErrorMessage = null,
                authConsentIntent = null,
                statusMessage = "로그아웃되었습니다."
            )
        }

        val prevCleanup = authCleanupJob
        authCleanupJob = viewModelScope.launch {
            prevCleanup?.join()
            prevSync?.join()
            prevUpload?.join()
            prevImage?.join()
            repository.clearCache()
        }
    }

    fun onGoogleSignInFailed(message: String) {
        _uiState.update {
            it.copy(
                errorMessage = "로그인 실패: $message",
                snackbarActionLabel = null,
                snackbarRetryAction = null
            )
        }
    }

    fun refreshFromDrive() {
        if (_uiState.value.isLoading || syncJob?.isActive == true) return
        val account = GoogleSignIn.getLastSignedInAccount(getApplication())
        if (account == null) {
            _uiState.update {
                it.copy(
                    errorMessage = "먼저 구글 계정으로 로그인해 주세요.",
                    snackbarActionLabel = null,
                    snackbarRetryAction = null
                )
            }
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
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    snackbarActionLabel = null,
                    snackbarRetryAction = null
                )
            }
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
                        errorMessage = err.message ?: "드라이브 동기화 중 오류가 발생했습니다.",
                        snackbarActionLabel = "다시 시도",
                        snackbarRetryAction = RetryAction.SYNC
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
        if (round == null) {
            dismissOriginalScorecardImage()
        }
        _uiState.update { it.copy(selectedRound = round) }
    }

    fun selectImage(uri: Uri?) {
        _uiState.update {
            it.copy(
                selectedImageUri = uri,
                errorMessage = null,
                snackbarActionLabel = null,
                snackbarRetryAction = null,
                statusMessage = null
            )
        }
    }

    fun uploadSelectedImage() {
        if (_uiState.value.isUploading || uploadJob?.isActive == true) return
        val uri = _uiState.value.selectedImageUri ?: return
        val account = GoogleSignIn.getLastSignedInAccount(getApplication())?.account
        if (account == null) {
            _uiState.update {
                it.copy(
                    errorMessage = "업로드하려면 먼저 구글 계정으로 로그인해 주세요.",
                    snackbarActionLabel = null,
                    snackbarRetryAction = null
                )
            }
            return
        }

        lateinit var job: Job
        job = viewModelScope.launch(start = CoroutineStart.LAZY) {
            authCleanupJob?.join()
            _uiState.update {
                it.copy(
                    isUploading = true,
                    errorMessage = null,
                    snackbarActionLabel = null,
                    snackbarRetryAction = null,
                    statusMessage = null
                )
            }
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
                        errorMessage = err.message ?: "업로드 중 오류가 발생했습니다.",
                        snackbarActionLabel = "다시 시도",
                        snackbarRetryAction = RetryAction.UPLOAD
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
        val roundForImage = pendingImageRound
        pendingConsentAction = null
        pendingImageRound = null
        if (!granted) {
            _uiState.update {
                if (action == ConsentPendingAction.VIEW_IMAGE) {
                    it.copy(imageErrorMessage = "Google Drive 권한 승인이 취소되었습니다.")
                } else {
                    it.copy(
                        errorMessage = "Google Drive 권한 승인이 취소되었습니다.",
                        snackbarActionLabel = null,
                        snackbarRetryAction = null
                    )
                }
            }
            return
        }
        when (action) {
            ConsentPendingAction.SYNC -> refreshFromDrive()
            ConsentPendingAction.UPLOAD -> uploadSelectedImage()
            ConsentPendingAction.VIEW_IMAGE -> roundForImage?.let { loadOriginalScorecardImage(it) }
            null -> Unit
        }
    }

    fun loadOriginalScorecardImage(round: GolfRound) {
        if (round.imageFileId.isBlank()) {
            _uiState.update {
                it.copy(
                    isViewerOpen = true,
                    isLoadingRoundImage = false,
                    viewingImageBytes = null,
                    imageErrorMessage = "연결된 원본 스코어카드 이미지가 없습니다."
                )
            }
            return
        }
        val account = GoogleSignIn.getLastSignedInAccount(getApplication())?.account
        if (account == null) {
            _uiState.update {
                it.copy(
                    isViewerOpen = true,
                    isLoadingRoundImage = false,
                    viewingImageBytes = null,
                    imageErrorMessage = "이미지를 보려면 먼저 로그인해 주세요."
                )
            }
            return
        }
        imageJob?.cancel()
        lateinit var job: Job
        job = viewModelScope.launch(start = CoroutineStart.LAZY) {
            _uiState.update {
                it.copy(
                    isViewerOpen = true,
                    isLoadingRoundImage = true,
                    viewingImageBytes = null,
                    imageErrorMessage = null
                )
            }
            val res = repository.downloadImageBytes(account, round.imageFileId)
            res.onSuccess { bytes ->
                _uiState.update {
                    it.copy(
                        isLoadingRoundImage = false,
                        viewingImageBytes = bytes,
                        imageErrorMessage = null
                    )
                }
            }.onFailure { err ->
                if (err is NeedAuthConsentException) {
                    pendingConsentAction = ConsentPendingAction.VIEW_IMAGE
                    pendingImageRound = round
                    _uiState.update {
                        it.copy(
                            isLoadingRoundImage = false,
                            authConsentIntent = err.consentIntent
                        )
                    }
                } else if (err !is CancellationException) {
                    _uiState.update {
                        it.copy(
                            isLoadingRoundImage = false,
                            imageErrorMessage = err.message ?: "이미지를 불러오지 못했습니다."
                        )
                    }
                }
            }
            if (imageJob === job) {
                imageJob = null
            }
        }
        imageJob = job
        job.start()
    }

    fun dismissOriginalScorecardImage() {
        imageJob?.cancel()
        imageJob = null
        _uiState.update {
            it.copy(
                isViewerOpen = false,
                isLoadingRoundImage = false,
                viewingImageBytes = null,
                imageErrorMessage = null
            )
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun clearErrorMessage() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                snackbarActionLabel = null,
                snackbarRetryAction = null
            )
        }
    }
}
