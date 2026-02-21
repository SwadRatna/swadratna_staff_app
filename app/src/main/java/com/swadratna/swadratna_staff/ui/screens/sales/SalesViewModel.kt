package com.swadratna.swadratna_staff.ui.screens.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_staff.data.remote.model.SalesResponse
import com.swadratna.swadratna_staff.data.remote.model.SalesSummary
import com.swadratna.swadratna_staff.data.remote.model.SaleTransaction
import com.swadratna.swadratna_staff.data.remote.repositories.SalesRepository
import com.swadratna.swadratna_staff.utils.permissions.PermissionManager
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
    data class Success(
        val sales: List<SaleTransaction>,
        val summary: SalesSummary,
        val totalCount: Int,
        val isLoadingMore: Boolean = false,
        val endReached: Boolean = false
    ) : SalesUiState()
    data class Error(val message: String) : SalesUiState()
}

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val repository: SalesRepository,
    val permissionManager: PermissionManager,
    private val networkMonitor: com.swadratna.swadratna_staff.utils.network.NetworkMonitor
) : ViewModel() {

    val permissions = permissionManager.currentUserPermissions
    val isOnline = networkMonitor.isOnline

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
    private var currentSalesList = mutableListOf<SaleTransaction>()
    
    init {
        fetchSales(isRefresh = true)
    }

    fun fetchSales(isRefresh: Boolean = false, isLoadMore: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _currentPage.value = 1
                currentSalesList.clear()
                _uiState.value = SalesUiState.Loading
            } else if (isLoadMore) {
                if (_uiState.value is SalesUiState.Success) {
                    val currentState = _uiState.value as SalesUiState.Success
                    if (currentState.isLoadingMore || currentState.endReached) return@launch
                    _uiState.value = currentState.copy(isLoadingMore = true)
                }
            } else {
                // Filter change case, treat as refresh
                _currentPage.value = 1
                currentSalesList.clear()
                _uiState.value = SalesUiState.Loading
            }
            
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
                limit = 20 // Using 20 as per user response example
            ).onSuccess { response ->
                val newSales = response.sales ?: emptyList()
                currentSalesList.addAll(newSales)
                
                val hasNext = response.pagination.hasNext
                val totalCount = response.pagination.totalCount
                
                if (hasNext) {
                    _currentPage.value += 1
                }
                
                _uiState.value = SalesUiState.Success(
                    sales = currentSalesList.toList(),
                    summary = response.summary,
                    totalCount = totalCount,
                    isLoadingMore = false,
                    endReached = !hasNext
                )
            }.onFailure {
                if (isLoadMore && _uiState.value is SalesUiState.Success) {
                    val currentState = _uiState.value as SalesUiState.Success
                    _uiState.value = currentState.copy(isLoadingMore = false)
                    // Optionally handle error toast here
                } else {
                    _uiState.value = SalesUiState.Error(it.message ?: "Unknown error")
                }
            }
        }
    }

    fun loadMore() {
        fetchSales(isLoadMore = true)
    }

    fun setDateFilter(date: String?) {
        _selectedDate.value = date
        _fromDate.value = null
        _toDate.value = null
        fetchSales(isRefresh = true)
    }

    fun setDateRangeFilter(from: String, to: String) {
        _selectedDate.value = null
        _fromDate.value = from
        _toDate.value = to
        fetchSales(isRefresh = true)
    }

    fun setFromDate(date: String) {
        _fromDate.value = date
        _selectedDate.value = null
        fetchSales(isRefresh = true)
    }

    fun setToDate(date: String) {
        _toDate.value = date
        _selectedDate.value = null
        fetchSales(isRefresh = true)
    }


    fun setYesterdayFilter() {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DATE, -1)
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        setDateFilter(date)
    }

    fun setTomorrowFilter() {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DATE, 1)
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        setDateFilter(date)
    }

    fun setThisWeekFilter() {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val from = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        
        val cal2 = java.util.Calendar.getInstance()
        val to = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal2.time)
        
        setDateRangeFilter(from, to)
    }

    fun setThisMonthFilter() {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        val from = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        
        val cal2 = java.util.Calendar.getInstance()
        val to = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal2.time)
        
        setDateRangeFilter(from, to)
    }

    fun setThisYearFilter() {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_YEAR, 1)
        val from = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
        
        val cal2 = java.util.Calendar.getInstance()
        val to = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal2.time)
        
        setDateRangeFilter(from, to)
    }

    fun setLifetimeFilter() {
        _selectedDate.value = null
        _fromDate.value = null
        _toDate.value = null
        fetchSales(isRefresh = true)
    }

    fun setOrderTypeFilter(type: String?) {
        _orderType.value = type
        fetchSales(isRefresh = true)
    }
    
    fun refresh() {
        fetchSales(isRefresh = true)
    }
}
