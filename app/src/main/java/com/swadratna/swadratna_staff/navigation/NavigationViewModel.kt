package com.swadratna.swadratna_staff.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_staff.data.local.dao.StaffUserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    staffUserDao: StaffUserDao
) : ViewModel() {

    val staffRole: StateFlow<String?> = staffUserDao.getLoggedInStaffUser()
        .map { it?.role }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
