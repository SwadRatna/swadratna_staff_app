package com.swadratna.swadratna_staff.ui.screens.profile


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_staff.data.local.dao.StaffUserDao
import com.swadratna.swadratna_staff.data.remote.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import com.swadratna.swadratna_staff.data.local.entities.StaffUser
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewmodel @Inject constructor(
    private val authRepository: AuthRepository,
    private val staffUserDao: StaffUserDao
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

    private val _staffUser = MutableStateFlow<StaffUser?>(null)
    val staffUser: StateFlow<StaffUser?> = _staffUser

    init {
        fetchStaffUserDetails()
    }

    fun onUsernameChange(newUsername: String) {
        _username.value = newUsername
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun fetchStaffUserDetails() {
        viewModelScope.launch {
            authRepository.getStaffUser().collect{ user ->
                _staffUser.value = user
            }
        }
    }

    fun logOut() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            authRepository.logout()
            staffUserDao.clearStaffUsers()
             _isLoading.value = false
        }
    }

    fun resetLoginState() {
        _loginSuccess.value = false
        _error.value = null
    }
}
