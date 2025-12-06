package com.swadratna.swadratna_staff.ui.orderDashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_staff.data.local.dao.StaffUserDao
import com.swadratna.swadratna_staff.data.remote.model.OrderListItem
import com.swadratna.swadratna_staff.data.remote.model.PaginationInfo
import com.swadratna.swadratna_staff.data.remote.repositories.OrderManagementRepository
import com.swadratna.swadratna_staff.utils.permissions.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.swadratna.swadratna_staff.utils.network.NetworkMonitor

@HiltViewModel
class OrdersDashboardViewModel @Inject constructor(
    private val orderManagementRepository: OrderManagementRepository,
    private val staffUserDao: StaffUserDao,
    val permissionManager: PermissionManager,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    
    val permissions = permissionManager.currentUserPermissions
    val isOnline = networkMonitor.isOnline

    val staffLocationId: StateFlow<Int?> = staffUserDao.getLoggedInStaffUser()
        .map { staffUser -> staffUser?.location?.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = null
        )

    private val _allOrders = MutableStateFlow<List<OrderListItem>>(emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    private val _selectedFilter = MutableStateFlow<String?>(null)
    val selectedFilter: StateFlow<String?> = _selectedFilter

    // Filtered orders based on search query and selected filter
    val orders: StateFlow<List<OrderListItem>> = combine(
        _allOrders,
        _searchQuery,
        _selectedFilter
    ) { allOrders, query, filter ->
        var filteredOrders = allOrders
        
        // Apply search filter
        if (query.isNotEmpty()) {
            filteredOrders = filteredOrders.filter { 
                it.id.toString().contains(query, ignoreCase = true) ||
                it.orderStatus.contains(query, ignoreCase = true)
            }
        }
        
        // Apply status filter
        when (filter) {
            null, "", "All" -> filteredOrders
            "Active" -> filteredOrders.filter { it.orderStatus != "completed" }
            "Completed" -> filteredOrders.filter { it.orderStatus == "completed" }
            else -> filteredOrders.filter { it.orderStatus == filter }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _paginationInfo = MutableStateFlow<PaginationInfo?>(null)
    val paginationInfo: StateFlow<PaginationInfo?> = _paginationInfo

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private var fetchJob: Job? = null


    init {
        fetchOrders()
    }

    fun fetchOrders(page: Int = 1, limit: Int = 10) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            try {
                // Collect the first value from staffLocationId flow
                val locationId = staffLocationId.first()
                
                if (locationId == null) {
                    _error.value = "Location ID not available"
                    _loading.value = false
                    return@launch
                }
                
                // Make the API call with the location ID
                val result = orderManagementRepository.getAllOrders(locationId, page, limit)
                
                result.onSuccess { response ->
                    val newOrders = response.orders
                    if (_allOrders.value != newOrders) {
                        _allOrders.value = newOrders
                        _paginationInfo.value = response.pagination
                    }
                    _loading.value = false
                }.onFailure { throwable ->
                    _error.value = throwable.message ?: "Unknown error occurred"
                    _loading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Failed to get location ID: ${e.message}"
                _loading.value = false
            }
        }
    }
    
    /**
     * Updates the search query and filters the orders accordingly
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * Updates the selected filter. If the same filter is selected again, it clears the filter.
     */
    fun updateFilter(filter: String) {
        _selectedFilter.value = if (_selectedFilter.value == filter) null else filter
    }
    
    /**
     * Clears all filters and search query
     */
    fun clearFilters() {
        _searchQuery.value = ""
        _selectedFilter.value = null
    }
    
    /**
     * Refreshes the orders list
     */
    fun refreshOrders() {
        fetchOrders(page = 1, limit = 50)
    }
}
