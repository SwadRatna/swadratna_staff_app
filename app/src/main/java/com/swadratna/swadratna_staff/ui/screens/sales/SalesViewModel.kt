package com.swadratna.swadratna_staff.ui.screens.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_staff.data.remote.model.SalesResponse
import com.swadratna.swadratna_staff.data.remote.repositories.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

sealed class SalesUiState {
    object Loading : SalesUiState()
    data class Success(val data: SalesResponse) : SalesUiState()
    data class Error(val message: String) : SalesUiState()
}

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val repository: SalesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SalesUiState>(SalesUiState.Loading)
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    // Filter states
    private val _selectedDate = MutableStateFlow<String?>(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val selectedDate: StateFlow<String?> = _selectedDate.asStateFlow()

    private val _fromDate = MutableStateFlow<String?>(null)
    val fromDate: StateFlow<String?> = _fromDate.asStateFlow()

    private val _toDate = MutableStateFlow<String?>(null)
    val toDate: StateFlow<String?> = _toDate.asStateFlow()

    private val _orderType = MutableStateFlow<String?>(null)
    val orderType: StateFlow<String?> = _orderType.asStateFlow()
    
    // Pagination
    private val _currentPage = MutableStateFlow(1)
    
    init {
        fetchSales()
    }

    fun fetchSales() {
        viewModelScope.launch {
            _uiState.value = SalesUiState.Loading
            
            val date = _selectedDate.value
            val from = _fromDate.value
            val to = _toDate.value
            val type = _orderType.value
            val page = _currentPage.value

            repository.getSales(
                date = date,
                fromDate = from,
                toDate = to,
                orderType = type,
                page = page,
                limit = 50 // Default limit
            ).onSuccess {
                _uiState.value = SalesUiState.Success(it)
            }.onFailure {
                _uiState.value = SalesUiState.Error(it.message ?: "Unknown error")
            }
        }
    }

    fun setDateFilter(date: String?) {
        _selectedDate.value = date
        _fromDate.value = null
        _toDate.value = null
        fetchSales()
    }

    fun setDateRangeFilter(from: String, to: String) {
        _selectedDate.value = null
        _fromDate.value = from
        _toDate.value = to
        fetchSales()
    }

    fun setOrderTypeFilter(type: String?) {
        _orderType.value = type
        fetchSales()
    }
    
    fun refresh() {
        fetchSales()
    }
}
