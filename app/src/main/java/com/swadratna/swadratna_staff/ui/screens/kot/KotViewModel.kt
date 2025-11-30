package com.swadratna.swadratna_staff.ui.screens.kot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_staff.data.local.dao.StaffUserDao
import com.swadratna.swadratna_staff.data.remote.model.KotItemX
import com.swadratna.swadratna_staff.data.remote.model.KotStatusUpdateResponse
import com.swadratna.swadratna_staff.data.remote.repositories.KotRepository
import com.swadratna.swadratna_staff.utils.permissions.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class KotListState {
    object Loading : KotListState()
    data class Success(val kots: List<KotItemX>) : KotListState()
    data class Error(val message: String) : KotListState()
    object Empty : KotListState()
}

sealed class KotStatusUpdateState {
    object Idle : KotStatusUpdateState()
    object Loading : KotStatusUpdateState()
    data class Success(val response: KotStatusUpdateResponse) : KotStatusUpdateState()
    data class Error(val message: String) : KotStatusUpdateState()
}

@HiltViewModel
class KotViewModel @Inject constructor(
    private val kotRepository: KotRepository,
    private val staffUserDao: StaffUserDao,
    val permissionManager: PermissionManager
) : ViewModel() {

    val permissions = permissionManager.currentUserPermissions

    private val _kotListState = MutableStateFlow<KotListState>(KotListState.Loading)
    val kotListState: StateFlow<KotListState> = _kotListState.asStateFlow()

    private val _kotStatusUpdateState = MutableStateFlow<KotStatusUpdateState>(KotStatusUpdateState.Idle)
    val kotStatusUpdateState: StateFlow<KotStatusUpdateState> = _kotStatusUpdateState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedKot = MutableStateFlow<KotItemX?>(null)
    val selectedKot: StateFlow<KotItemX?> = _selectedKot.asStateFlow()

    private val _showOnlyPending = MutableStateFlow(false)
    val showOnlyPending: StateFlow<Boolean> = _showOnlyPending.asStateFlow()

    // StateFlow to hold the current staff user's location ID
    val staffLocationId: StateFlow<Int?> = staffUserDao.getLoggedInStaffUser()
        .map { staffUser -> staffUser?.location?.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun loadKots() {
        viewModelScope.launch {
            val locationId = staffLocationId.first()
            if (locationId != null) {
                _kotListState.value = KotListState.Loading
                kotRepository.getKots(locationId, _showOnlyPending.value)
                    .onSuccess { response ->
                        if (response.kots.isEmpty()) {
                            _kotListState.value = KotListState.Empty
                        } else {
                            _kotListState.value = KotListState.Success(response.kots)
                        }
                    }
                    .onFailure { error ->
                        _kotListState.value = KotListState.Error(error.message ?: "Unknown error")
                    }
            } else {
                _kotListState.value = KotListState.Error("Location ID not found")
            }
        }
    }

    fun updateKotStatus(kotId: Int, newStatus: String) {
        viewModelScope.launch {
            _kotStatusUpdateState.value = KotStatusUpdateState.Loading
            kotRepository.updateKotStatus(kotId, newStatus)
                .onSuccess { response ->
                    _kotStatusUpdateState.value = KotStatusUpdateState.Success(response)
                    // Refresh the list after successful update
                    loadKots()
                }
                .onFailure { error ->
                    _kotStatusUpdateState.value = KotStatusUpdateState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun setSelectedKot(kot: KotItemX?) {
        _selectedKot.value = kot
    }

    fun resetStatusUpdateState() {
        _kotStatusUpdateState.value = KotStatusUpdateState.Idle
    }

    fun setShowOnlyPending(showOnlyPending: Boolean) {
        _showOnlyPending.value = showOnlyPending
        loadKots()
    }

    fun refreshKots() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadKots()
            _isRefreshing.value = false
        }
    }

    init {
        // Automatically load KOTs when location ID is available
        viewModelScope.launch {
            staffLocationId.collect { locationId ->
                locationId?.let {
                    loadKots()
                }
            }
        }
    }
}