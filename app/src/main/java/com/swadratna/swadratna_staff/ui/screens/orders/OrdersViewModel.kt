package com.swadratna.swadratna_staff.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_staff.data.local.dao.StaffUserDao

import com.swadratna.swadratna_staff.data.remote.model.OrderDetailsX
import com.swadratna.swadratna_staff.data.remote.repositories.OrderManagementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OrderDetailsXState {
    object Loading : OrderDetailsXState()
    data class Success(val order: OrderDetailsX) : OrderDetailsXState()
    data class Error(val message: String) : OrderDetailsXState()
    object Idle : OrderDetailsXState()
}

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val repository: OrderManagementRepository,
    private val staffUserDao: StaffUserDao // Assuming this is needed for X-Key or other user info
) : ViewModel() {

    private val _detailedOrderState = MutableStateFlow<OrderDetailsXState>(OrderDetailsXState.Idle)
    val detailedOrderState: StateFlow<OrderDetailsXState> = _detailedOrderState.asStateFlow()

    fun getOrderDetail(orderID: String) {
        viewModelScope.launch {
            _detailedOrderState.value = OrderDetailsXState.Loading
            repository.getOrderDetail(orderID)
                .onSuccess {
                    _detailedOrderState.value = OrderDetailsXState.Success(it)
                }
                .onFailure {
                    _detailedOrderState.value = OrderDetailsXState.Error(it.message ?: "Unknown error fetching order details")
                }
        }
    }

    fun resetDetailedOrderState() {
        _detailedOrderState.value = OrderDetailsXState.Idle
    }
}