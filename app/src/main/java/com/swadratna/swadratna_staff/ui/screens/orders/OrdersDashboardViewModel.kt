package com.swadratna.swadratna_staff.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_staff.data.local.dao.StaffUserDao
import com.swadratna.swadratna_staff.data.remote.model.OrderListItem
import com.swadratna.swadratna_staff.data.remote.model.PaginationInfo
import com.swadratna.swadratna_staff.data.remote.repositories.OrderManagementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersDashboardViewModel @Inject constructor(
    private val orderManagementRepository: OrderManagementRepository,
    private val staffUserDao: StaffUserDao
) : ViewModel() {
    val staffLocationId: StateFlow<Int?> = staffUserDao.getLoggedInStaffUser()
        .map { staffUser -> staffUser?.location?.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _orders = MutableStateFlow<List<OrderListItem>>(emptyList())
    val orders: StateFlow<List<OrderListItem>> = _orders

    private val _paginationInfo = MutableStateFlow<PaginationInfo?>(null)
    val paginationInfo: StateFlow<PaginationInfo?> = _paginationInfo

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchOrders( page: Int, limit: Int) {

        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            staffLocationId.collect { locationId ->
                locationId?.let {
                    val result = orderManagementRepository.getAllOrders(staffLocationId.value!!, page, limit)
                    result.onSuccess {
                        _orders.value = it.orders
                        _paginationInfo.value = it.pagination
                    }.onFailure {
                        _error.value = it.message
                    }
                }
            }
            _loading.value = false
        }
    }
}
