package com.swadratna.swadratna_staff.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_staff.data.remote.repositories.AuthRepository
import com.swadratna.swadratna_staff.data.remote.services.LoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun onUsernameChange(newUsername: String) {
        _username.value = newUsername
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun login() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val request = LoginRequest(username.value, password.value)
            authRepository.login(request)
                .onSuccess { response ->
                    _loginSuccess.value = true
                    _isLoading.value = false
                }
                .onFailure { throwable ->
                    _error.value = throwable.message ?: "Unknown error"
                    _isLoading.value = false
                }
        }
    }

    fun resetLoginState() {
        _loginSuccess.value = false
        _error.value = null
    }
}
