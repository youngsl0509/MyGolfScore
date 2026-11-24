package com.mygolfscore.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.mygolfscore.app.data.drive.GoogleDriveRepository
import com.mygolfscore.app.data.model.GolfRound
import com.mygolfscore.app.data.model.GolfScorePayload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
)

class GolfScoreViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GoogleDriveRepository(application.applicationContext)

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
        _uiState.update {
            it.copy(
                isSignedIn = true,
                accountEmail = account.email,
                accountDisplayName = account.displayName,
                accountPhotoUrl = account.photoUrl?.toString(),
                errorMessage = null,
                statusMessage = "구글 계정 연결됨 (${account.email})"
            )
        }
        syncFromDrive(account)
    }

    fun onGoogleSignOutSuccess() {
        val sample = runCatching { repository.loadLocalSample() }.getOrDefault(GolfScorePayload())
        _uiState.update {
            it.copy(
                isSignedIn = false,
                accountEmail = null,
                accountDisplayName = null,
                accountPhotoUrl = null,
                isUsingOfflineSample = true,
                payload = sample,
                selectedRound = null,
                selectedImageUri = null,
                statusMessage = "로그아웃되었습니다."
            )
        }
    }

    fun onGoogleSignInFailed(message: String) {
        _uiState.update {
            it.copy(errorMessage = "로그인 실패: $message")
        }
    }

    fun refreshFromDrive() {
        if (_uiState.value.isLoading) return
        val account = GoogleSignIn.getLastSignedInAccount(getApplication())
        if (account == null) {
            _uiState.update { it.copy(errorMessage = "먼저 구글 계정으로 로그인해 주세요.") }
            return
        }
        syncFromDrive(account)
    }

    private fun syncFromDrive(account: GoogleSignInAccount) {
        val gAccount = account.account ?: return
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val token = repository.fetchAccessToken(gAccount)
                repository.downloadScoreJson(token).getOrThrow()
            }.onSuccess { downloaded ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isUsingOfflineSample = false,
                        payload = downloaded,
                        statusMessage = "구글 드라이브 최신 데이터 동기화 완료 (${downloaded.rounds.size}경기)"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.message ?: "드라이브 동기화 중 오류가 발생했습니다."
                    )
                }
            }
        }
    }

    fun selectRound(round: GolfRound?) {
        _uiState.update { it.copy(selectedRound = round) }
    }

    fun selectImage(uri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri, errorMessage = null, statusMessage = null) }
    }

    fun uploadSelectedImage() {
        if (_uiState.value.isUploading) return
        val uri = _uiState.value.selectedImageUri ?: return
        val account = GoogleSignIn.getLastSignedInAccount(getApplication())?.account
        if (account == null) {
            _uiState.update { it.copy(errorMessage = "업로드하려면 먼저 구글 계정으로 로그인해 주세요.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, errorMessage = null, statusMessage = null) }
            runCatching {
                val token = repository.fetchAccessToken(account)
                repository.uploadScorecardImage(token, uri).getOrThrow()
            }.onSuccess { uploadedName ->
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        selectedImageUri = null,
                        statusMessage = "GolfScore/Image 폴더에 업로드 완료: $uploadedName"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        errorMessage = err.message ?: "업로드 중 오류가 발생했습니다."
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }
}
