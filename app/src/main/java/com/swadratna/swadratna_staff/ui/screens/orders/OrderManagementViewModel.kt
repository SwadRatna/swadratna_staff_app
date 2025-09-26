package com.swadratna.swadratna_staff.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_staff.data.remote.model.CustomerBill
import com.swadratna.swadratna_staff.data.remote.model.MenuItem
import com.swadratna.swadratna_staff.data.remote.model.Table
import com.swadratna.swadratna_staff.data.remote.repositories.OrderManagementRepository
import com.swadratna.swadratna_staff.data.remote.services.KotRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderManagementViewModel @Inject constructor(
    private val repository: OrderManagementRepository
) : ViewModel() {

    private val _tables = MutableStateFlow<List<Table>>(emptyList())
    val tables: StateFlow<List<Table>> = _tables

    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems

    private val _currentBill = MutableStateFlow<CustomerBill?>(null)
    val currentBill: StateFlow<CustomerBill?> = _currentBill

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun getTables(locationId: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            repository.getTablesByLocation(locationId)
                .onSuccess { _tables.value = it }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun getMenuItems(locationId: Int) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            repository.getMenuByLocation(locationId)
                .onSuccess { _menuItems.value = it }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun createKot(kotRequest: KotRequest) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            repository.createKot(kotRequest)
                .onSuccess { /* Handle success, maybe refresh bill or tables */ }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun findBill(orderId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            repository.findBill(orderId)
                .onSuccess { _currentBill.value = it }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun approveBill(orderId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            repository.approveBill(orderId)
                .onSuccess { /* Handle success, maybe clear current bill or refresh tables */ }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }
}
