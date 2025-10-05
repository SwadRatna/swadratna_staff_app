package com.swadratna.swadratna_staff.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.swadratna.swadratna_staff.data.remote.model.Order
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    // Assuming an OrderRepository will be injected here later
) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchOrders()
    }

    fun fetchOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            delay(2000) // Simulate network delay
            _orders.value = listOf(
                Order("#1001", "Allan Johnson", listOf("Spaghetti bolognese", "Water"), "New"),
                Order("#1002", "Bob Williams", listOf("Chicken Burger", "Coca-cola"), "New"),
                Order("#1003", "Charlie Brown", listOf("Veggie Burger", "French Fries", "Water"), "In Progress"),
                Order("#1004", "Diana Prince", listOf("Pizza Margherita", "Sprite"), "In Progress"),
                Order("#1005", "Harry Eagle", listOf("Tuna Pizza", "Fanta Orange", "Water"), "Completed")
            )
            _isLoading.value = false
        }
    }
}
