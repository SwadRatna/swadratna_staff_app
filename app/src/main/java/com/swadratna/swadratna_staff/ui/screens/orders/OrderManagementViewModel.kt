package com.swadratna.swadratna_staff.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_staff.data.local.dao.StaffUserDao
import com.swadratna.swadratna_staff.data.remote.model.Category
import com.swadratna.swadratna_staff.data.remote.model.CustomerBill
import com.swadratna.swadratna_staff.data.remote.model.MenuItem
import com.swadratna.swadratna_staff.data.remote.model.Customer
import com.swadratna.swadratna_staff.data.remote.model.KotRequest
import com.swadratna.swadratna_staff.data.remote.model.LineItem
import com.swadratna.swadratna_staff.data.remote.model.OccupyTableRequest
import com.swadratna.swadratna_staff.data.remote.model.OccupyTableResponse
import com.swadratna.swadratna_staff.data.remote.model.TableListResponse
import com.swadratna.swadratna_staff.data.remote.repositories.OrderManagementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TableListState {
    object Loading : TableListState()
    data class Success(val tables: TableListResponse) : TableListState()
    data class Error(val message: String) : TableListState()
}

sealed class CustomerState {
    object Idle : CustomerState()
    object Loading : CustomerState()
    data class Success(val customer: Customer) : CustomerState()
    data class Error(val message: String) : CustomerState()
}

sealed class OccupyTableState {
    object Idle : OccupyTableState()
    object Loading : OccupyTableState()
    data class Success(val response: OccupyTableResponse) : OccupyTableState()
    data class Error(val message: String) : OccupyTableState()
}

sealed class OrderConfirmationState {
    object Idle : OrderConfirmationState()
    object Loading : OrderConfirmationState()
    object Success : OrderConfirmationState()
    data class Error(val message: String) : OrderConfirmationState()
}

@HiltViewModel
class OrderManagementViewModel @Inject constructor(
    private val repository: OrderManagementRepository,
    private val staffUserDao: StaffUserDao
) : ViewModel() {

    private val _tableListState = MutableStateFlow<TableListState>(TableListState.Loading)
    val tableListState: StateFlow<TableListState> = _tableListState

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _menuItemsMap = MutableStateFlow<Map<String, List<MenuItem>>>(emptyMap())
    val menuItemsMap: StateFlow<Map<String, List<MenuItem>>> = _menuItemsMap.asStateFlow()

    private val _currentOrderItems = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val currentOrderItems: StateFlow<Map<Int, Int>> = _currentOrderItems.asStateFlow()

    private val _currentBill = MutableStateFlow<CustomerBill?>(null)
    val currentBill: StateFlow<CustomerBill?> = _currentBill

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _customerState = MutableStateFlow<CustomerState>(CustomerState.Idle)
    val customerState: StateFlow<CustomerState> = _customerState

    private val _occupyTableState = MutableStateFlow<OccupyTableState>(OccupyTableState.Idle)
    val occupyTableState: StateFlow<OccupyTableState> = _occupyTableState

    private val _orderConfirmationState = MutableStateFlow<OrderConfirmationState>(OrderConfirmationState.Idle)
    val orderConfirmationState: StateFlow<OrderConfirmationState> = _orderConfirmationState

    fun getOrCreateCustomer(mobile: String?, userName: String?) {
        viewModelScope.launch {
            _customerState.value = CustomerState.Loading
            repository.getOrCreateCustomer(mobile, userName)
                .onSuccess { _customerState.value = CustomerState.Success(it) }
                .onFailure { _customerState.value = CustomerState.Error(it.message ?: "Unknown error") }
        }
    }

    fun occupyTable(tableId: Int, customerId: Int) {
        viewModelScope.launch {
            _occupyTableState.value = OccupyTableState.Loading
            val request = OccupyTableRequest(tableId, customerId)
            repository.occupyTable(request)
                .onSuccess { _occupyTableState.value = OccupyTableState.Success(it) }
                .onFailure { _occupyTableState.value = OccupyTableState.Error(it.message ?: "Unknown error") }
        }
    }

    fun resetCustomerState() {
        _customerState.value = CustomerState.Idle
    }

    fun resetOccupyTableState() {
        _occupyTableState.value = OccupyTableState.Idle
    }

    fun updateOrderItem(menuItemId: Int, quantity: Int) {
        _currentOrderItems.update { currentItems ->
            val newItems = currentItems.toMutableMap()
            if (quantity > 0) {
                newItems[menuItemId] = quantity
            } else {
                newItems.remove(menuItemId)
            }
            newItems
        }
    }

    fun confirmOrder(orderId: Int) {
        viewModelScope.launch {
            _orderConfirmationState.value = OrderConfirmationState.Loading
            val kotItems = _currentOrderItems.value.map { (itemId, quantity) ->
                LineItem(menuItemId = itemId, quantity = quantity , instructions = "")
            }
            val kotRequest = KotRequest(orderId = orderId, lineItems = kotItems)
            repository.createKot(kotRequest)
                .onSuccess {
                    _orderConfirmationState.value = OrderConfirmationState.Success
                    _currentOrderItems.value = emptyMap()
                }
                .onFailure { _orderConfirmationState.value = OrderConfirmationState.Error(it.message ?: "Unknown error") }
        }
    }

    fun resetOrderConfirmationState() {
        _orderConfirmationState.value = OrderConfirmationState.Idle
    }

    // StateFlow to hold the current staff user's location ID
    val staffLocationId: StateFlow<Int?> = staffUserDao.getLoggedInStaffUser()
        .map { staffUser -> staffUser?.location?.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        // Automatically call getTables when staffLocationId changes and is not null
        viewModelScope.launch {
            staffLocationId.collect { locationId ->
                locationId?.let {
                    getTables(it)
                }
            }
        }
    }

    fun getTables(locationId: Int) {
        viewModelScope.launch {
            _isRefreshing.value = true // Set refreshing to true
            _tableListState.value = TableListState.Loading // Emit Loading state
            repository.getTablesByLocation(locationId)
                .onSuccess { _tableListState.value = TableListState.Success(it) } // Emit Success state
                .onFailure { _tableListState.value = TableListState.Error(it.message ?: "Unknown error") } // Emit Error state
            _isRefreshing.value = false
        }
    }

    fun getMenuItems( searchQuery: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            staffLocationId.value?.let {
                val result = repository.getMenu(it, searchQuery)

                result.onSuccess { response ->
                    _categories.value = response.categories
                    _menuItemsMap.value = response.menuItems
                    _loading.value = false

                }.onFailure { exception ->
                    _error.value = exception.message
                    _loading.value = false
                }
            }
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


//    fun approveBill(orderId: String) {
//        viewModelScope.launch {
//            _loading.value = true
//            _error.value = null
//            repository.approveBill(orderId)
//                .onSuccess { /* Handle success, maybe clear current bill or refresh tables */ }
//                .onFailure { _error.value = it.message }
//            _loading.value = false
//        }
//    }
}
