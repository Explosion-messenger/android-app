package com.explosion.messenger.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.explosion.messenger.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String, isPasswordless: Boolean) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            if (isPasswordless) {
                // In our flow, passwordless-init just moves to 2FA screen
                // Real bypass happens when code is entered.
                // For now, we just skip to 2FA state.
                _uiState.value = AuthUiState.Requires2FA(username, isPasswordless = true)
            } else {
                repository.login(username, password)
                    .onSuccess { response ->
                        if (response.requires_2fa) {
                            _uiState.value = AuthUiState.Requires2FA(username, isPasswordless = false)
                        } else {
                            _uiState.value = AuthUiState.Success
                        }
                    }
                    .onFailure { error ->
                        _uiState.value = AuthUiState.Error(error.message ?: "Unknown error")
                    }
            }
        }
    }

    fun verify2fa(username: String, code: String, isPasswordless: Boolean) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = if (isPasswordless) {
                repository.loginPasswordless(username, code)
            } else {
                repository.verify2fa(username, code)
            }
            
            result.onSuccess {
                _uiState.value = AuthUiState.Success
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "2FA failed")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Requires2FA(val username: String, val isPasswordless: Boolean) : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
