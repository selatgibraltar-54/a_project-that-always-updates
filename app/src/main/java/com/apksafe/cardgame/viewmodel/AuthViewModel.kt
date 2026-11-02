package com.apksafe.cardgame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.apksafe.cardgame.data.db.MatchHistoryEntity
import com.apksafe.cardgame.data.db.UserEntity
import com.apksafe.cardgame.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val currentUser: UserEntity? = null,
    val isRegisterMode: Boolean = false,
    val selectedAvatar: String = "dragon",
    val error: String? = null,
    val successMessage: String? = null,
    val isLoading: Boolean = false
)

class AuthViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val matchHistory: StateFlow<List<MatchHistoryEntity>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleMode(isRegister: Boolean) {
        _uiState.update { it.copy(isRegisterMode = isRegister, error = null, successMessage = null) }
    }

    fun selectAvatar(avatar: String) {
        _uiState.update { it.copy(selectedAvatar = avatar) }
    }

    fun login(username: String, pin: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            val result = repository.loginUser(username, pin)
            result.onSuccess { user ->
                _uiState.update { it.copy(currentUser = user, isLoading = false, error = null) }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, error = err.message ?: "Login failed") }
            }
        }
    }

    fun register(username: String, pin: String, originDeviceId: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            val result = repository.registerUser(username, pin, _uiState.value.selectedAvatar, originDeviceId)
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        currentUser = user,
                        isLoading = false,
                        error = null,
                        successMessage = "Akun '${user.username}' berhasil dibuat!"
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, error = err.message ?: "Registrasi gagal") }
            }
        }
    }

    fun quickSelectUser(user: UserEntity) {
        _uiState.update { it.copy(currentUser = user, error = null) }
    }

    fun logout() {
        _uiState.update { it.copy(currentUser = null, error = null) }
    }

    fun recordDemoMatch(isWin: Boolean) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            val opponentNames = listOf("AeroMaster", "ShadowDuelist", "FlameCaller", "AquaKnight", "VoltTitan")
            val opponent = opponentNames.random()
            val rounds = (3..9).random()
            val afkCount = (0..2).random()
            val reason = if (afkCount > 0 && (0..1).random() == 1) "AFK_FORFEIT" else "KO"
            val winner = if (isWin) user.username else opponent
            val loser = if (isWin) opponent else user.username

            repository.recordMatch(
                winnerUsername = winner,
                loserUsername = loser,
                rounds = rounds,
                afkSwitchesCount = afkCount,
                reason = reason
            )

            // Refresh current user data
            val updated = repository.getUserById(user.id)
            if (updated != null) {
                _uiState.update { it.copy(currentUser = updated) }
            }
        }
    }

    fun refreshCurrentUser() {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            val updated = repository.getUserById(user.id)
            if (updated != null) {
                _uiState.update { it.copy(currentUser = updated) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(repository) as T
        }
    }
}
