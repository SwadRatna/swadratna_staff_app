package com.swadratna.swadratna_staff.ui.screens.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.swadratna.swadratna_staff.data.remote.model.Staff
import com.swadratna.swadratna_staff.data.remote.model.AttendanceModifyRequest
import com.swadratna.swadratna_staff.data.remote.repositories.StaffRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AttendanceState {
    object Loading : AttendanceState()
    data class Success(val staffList: List<Staff>) : AttendanceState()
    data class Error(val message: String) : AttendanceState()
}

sealed class AttendanceUiEvent {
    data class ShowMessage(val message: String) : AttendanceUiEvent()
}

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val staffRepository: StaffRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AttendanceState>(AttendanceState.Loading)
    val state: StateFlow<AttendanceState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AttendanceUiEvent>()
    val uiEvent: SharedFlow<AttendanceUiEvent> = _uiEvent.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredStaffList: StateFlow<List<Staff>> = kotlinx.coroutines.flow.combine(
        _state,
        _searchQuery
    ) { state, query ->
        if (state is AttendanceState.Success) {
            if (query.isBlank()) {
                state.staffList
            } else {
                state.staffList.filter { staff ->
                    staff.name.contains(query, ignoreCase = true)
                }
            }
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, WhileSubscribed(5000), emptyList())

    init {
        fetchStaffMembers()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun fetchStaffMembers() {
        viewModelScope.launch {
            _state.value = AttendanceState.Loading
            staffRepository.getStaffMembers()
                .onSuccess { staffList ->
                    _state.value = AttendanceState.Success(staffList)
                }
                .onFailure { error ->
                    _state.value = AttendanceState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun checkIn(staffId: Int) {
        viewModelScope.launch {
            staffRepository.checkIn(staffId)
                .onSuccess {
                    _uiEvent.emit(AttendanceUiEvent.ShowMessage(it))
                    fetchStaffMembers() // Refresh list to update UI state
                }
                .onFailure { error ->
                    Log.e("AttendanceViewModel", "Check-in failed: ${error.message}", error)
                    
                    // If the error indicates "already checked in", we should refresh the list
                    // because our local state is likely out of sync with the server.
                    // The server says they are checked in, but our UI thought they weren't.
                    if (error.message?.contains("already checked in", ignoreCase = true) == true) {
                        _uiEvent.emit(AttendanceUiEvent.ShowMessage("Syncing status: Staff was already checked in."))
                        fetchStaffMembers()
                    } else {
                        _uiEvent.emit(AttendanceUiEvent.ShowMessage(error.message ?: "Check-in failed"))
                    }
                }
        }
    }

    fun checkOut(staffId: Int) {
        viewModelScope.launch {
            staffRepository.checkOut(staffId)
                .onSuccess {
                    _uiEvent.emit(AttendanceUiEvent.ShowMessage(it))
                    fetchStaffMembers() // Refresh list to update UI state
                }
                .onFailure { error ->
                    Log.e("AttendanceViewModel", "Check-out failed: ${error.message}", error)
                    
                    // Similarly for check-out, if they are already checked out, refresh.
                    if (error.message?.contains("already checked out", ignoreCase = true) == true) {
                        _uiEvent.emit(AttendanceUiEvent.ShowMessage("Syncing status: Staff was already checked out."))
                        fetchStaffMembers()
                    } else {
                        _uiEvent.emit(AttendanceUiEvent.ShowMessage(error.message ?: "Check-out failed"))
                    }
                }
        }
    }

    fun modifyAttendance(id: Int, request: AttendanceModifyRequest) {
        viewModelScope.launch {
            staffRepository.modifyAttendance(id, request)
                .onSuccess {
                    _uiEvent.emit(AttendanceUiEvent.ShowMessage(it))
                    fetchStaffMembers() // Refresh list to update UI state
                }
                .onFailure {
                    Log.e("AttendanceViewModel", "Modification failed: ${it.message}", it)
                    _uiEvent.emit(AttendanceUiEvent.ShowMessage(it.message ?: "Modification failed"))
                }
        }
    }
}
