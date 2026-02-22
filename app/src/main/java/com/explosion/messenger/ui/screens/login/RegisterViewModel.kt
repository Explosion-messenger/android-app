package com.explosion.messenger.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.explosion.messenger.data.remote.ApiService
import com.explosion.messenger.data.remote.UserCreate
import com.explosion.messenger.data.remote.UserRegisterConfirm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun setupRegistration(username: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            try {
                val response = api.registerSetup(UserCreate(username, email.takeIf { it.isNotBlank() }, password))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _uiState.value = RegisterUiState.Requires2FAConfirm(
                        username = username,
                        email = email,
                        password = password,
                        secret = body.secret,
                        otpAuthUrl = body.otp_auth_url
                    )
                } else {
                    _uiState.value = RegisterUiState.Error("Registration setup failed. Username may be taken.")
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState.Error(e.message ?: "Network error during registration setup.")
            }
        }
    }

    fun confirmRegistration(state: RegisterUiState.Requires2FAConfirm, code: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            try {
                val response = api.registerConfirm(
                    UserRegisterConfirm(
                        username = state.username,
                        email = state.email.takeIf { it.isNotBlank() },
                        password = state.password,
                        secret = state.secret,
                        code = code
                    )
                )
                if (response.isSuccessful) {
                    _uiState.value = RegisterUiState.Success
                } else {
                    // Go back to the 2FA state so they can try the code again
                    _uiState.value = RegisterUiState.Error("Invalid 2FA Code")
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState.Error(e.message ?: "Network error during confirmation.")
            }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Requires2FAConfirm(
        val username: String,
        val email: String,
        val password: String,
        val secret: String,
        val otpAuthUrl: String
    ) : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}
