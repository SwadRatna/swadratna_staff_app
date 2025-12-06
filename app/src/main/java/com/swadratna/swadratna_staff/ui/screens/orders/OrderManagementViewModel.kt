package com.swadratna.swadratna_staff.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_staff.data.local.dao.StaffUserDao
import com.swadratna.swadratna_staff.data.remote.model.Category
import com.swadratna.swadratna_staff.data.remote.model.CustomerBill
import com.swadratna.swadratna_staff.data.remote.model.Customer
import com.swadratna.swadratna_staff.data.remote.model.KotRequest
import com.swadratna.swadratna_staff.data.remote.model.LineItem
import com.swadratna.swadratna_staff.data.remote.model.MenuItem
import com.swadratna.swadratna_staff.data.remote.model.OccupyTableRequest
import com.swadratna.swadratna_staff.data.remote.model.OccupyTableResponse
import com.swadratna.swadratna_staff.data.remote.model.TableListResponse
import com.swadratna.swadratna_staff.data.remote.repositories.OrderManagementRepository
import com.swadratna.swadratna_staff.data.remote.model.OrderDetailsX
import com.swadratna.swadratna_staff.data.remote.model.BillDetail
import com.swadratna.swadratna_staff.data.remote.model.FreeTableResponse
import com.swadratna.swadratna_staff.utils.permissions.PermissionManager
import com.swadratna.swadratna_staff.utils.network.NetworkMonitor

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
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

sealed class OrderDetailsXState {
    object Loading : OrderDetailsXState()
    data class Success(val order: OrderDetailsX) : OrderDetailsXState()
    data class Error(val message: String) : OrderDetailsXState()
    object Idle : OrderDetailsXState()
}

sealed class BillDetailsState {
    object Loading : BillDetailsState()
    data class Success(val billDetail: BillDetail) : BillDetailsState()
    data class Error(val message: String) : BillDetailsState()
    object Idle : BillDetailsState()
}

sealed class FreeTableState {
    object Idle : FreeTableState()
    object Loading : FreeTableState()
    data class Success(val response: FreeTableResponse) : FreeTableState()
    data class Error(val message: String) : FreeTableState()
}

sealed class CreateParcelOrderState {
    object Idle : CreateParcelOrderState()
    object Loading : CreateParcelOrderState()
    data class Success(val response: com.swadratna.swadratna_staff.data.remote.services.CreateOrderResponse) : CreateParcelOrderState()
    data class Error(val message: String) : CreateParcelOrderState()
}

@HiltViewModel
class OrderManagementViewModel @Inject constructor(
    private val repository: OrderManagementRepository,
    private val staffUserDao: StaffUserDao,
    val permissionManager: PermissionManager,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    val permissions = permissionManager.currentUserPermissions
    val isOnline = networkMonitor.isOnline

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

    private val _detailedOrderState = MutableStateFlow<OrderDetailsXState>(OrderDetailsXState.Idle)
    val detailedOrderState: StateFlow<OrderDetailsXState> = _detailedOrderState.asStateFlow()

    private val _billDetailsState = MutableStateFlow<BillDetailsState>(BillDetailsState.Idle)
    val billDetailsState: StateFlow<BillDetailsState> = _billDetailsState.asStateFlow()

    private val _freeTableState = MutableStateFlow<FreeTableState>(FreeTableState.Idle)
    val freeTableState: StateFlow<FreeTableState> = _freeTableState.asStateFlow()

    private val _createParcelOrderState = MutableStateFlow<CreateParcelOrderState>(CreateParcelOrderState.Idle)
    val createParcelOrderState: StateFlow<CreateParcelOrderState> = _createParcelOrderState.asStateFlow()

    private val _paymentSuccess = kotlinx.coroutines.flow.MutableSharedFlow<com.swadratna.swadratna_staff.data.remote.services.RecordPaymentResponse>()
    val paymentSuccess = _paymentSuccess.asSharedFlow()

    private val _staffRole = MutableStateFlow<String?>(null)
    val staffRole: StateFlow<String?> = _staffRole.asStateFlow()

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
            repository.createKot( kotRequest) // Added X-Key
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

    // StateFlow to hold the current staff user
    val currentStaffUser: StateFlow<com.swadratna.swadratna_staff.data.local.entities.StaffUser?> = staffUserDao.getLoggedInStaffUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        viewModelScope.launch {
            staffLocationId.collect { locationId ->
                locationId?.let {
                    getTables(it)
                }
            }
        }
        fetchStaffRole()
    }

    fun getTables(locationId: Int) {
        viewModelScope.launch {
            _isRefreshing.value = true // Set refreshing to true
            if (_tableListState.value !is TableListState.Success) {
                _tableListState.value = TableListState.Loading // Emit Loading state only if not already Success
            }
            repository.getTablesByLocation(locationId)
                .onSuccess { _tableListState.value = TableListState.Success(it) } // Emit Success state
                .onFailure { 
                    if (_tableListState.value !is TableListState.Success) {
                        _tableListState.value = TableListState.Error(it.message ?: "Unknown error") 
                    }
                } // Emit Error state only if not Success
            _isRefreshing.value = false
        }
    }

    fun getTablesSilent(locationId: Int) {
        viewModelScope.launch {
            repository.getTablesByLocation(locationId)
                .onSuccess { newResponse ->
                    val currentState = _tableListState.value
                    if (currentState is TableListState.Success) {
                        if (currentState.tables != newResponse) {
                            _tableListState.value = TableListState.Success(newResponse)
                        }
                    } else {
                        _tableListState.value = TableListState.Success(newResponse)
                    }
                }
                .onFailure {
                     if (_tableListState.value !is TableListState.Success) {
                         _tableListState.value = TableListState.Error(it.message ?: "Unknown error")
                    }
                }
        }
    }

    fun getMenuItems( staffLocationId:Int , searchQuery: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val result = repository.getMenu(staffLocationId, searchQuery)

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

    fun createKot(kotRequest: KotRequest) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            repository.createKot( kotRequest) // Added X-Key
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

    fun getOrderDetail(orderID: String) {
        viewModelScope.launch {
            if (_detailedOrderState.value !is OrderDetailsXState.Success) {
                _detailedOrderState.value = OrderDetailsXState.Loading
            }
            repository.getOrderDetail(orderID)
                .onSuccess {
                    _detailedOrderState.value = OrderDetailsXState.Success(it)
                }
                .onFailure {
                    if (_detailedOrderState.value !is OrderDetailsXState.Success) {
                        _detailedOrderState.value = OrderDetailsXState.Error(it.message ?: "Unknown error fetching order details")
                    }
                }
        }
    }

    fun resetDetailedOrderState() {
        _detailedOrderState.value = OrderDetailsXState.Idle
    }

    fun getBillDetails(orderId: String) {
        viewModelScope.launch {
            if (_billDetailsState.value !is BillDetailsState.Success) {
                _billDetailsState.value = BillDetailsState.Loading
            }
            repository.getBillDetails(orderId)
                .onSuccess {
                    _billDetailsState.value = BillDetailsState.Success(it)
                }
                .onFailure {
                    if (_billDetailsState.value !is BillDetailsState.Success) {
                        _billDetailsState.value = BillDetailsState.Error(it.message ?: "Unknown error fetching bill details")
                    }
                }
        }
    }

    fun fetchStaffRole() {
        viewModelScope.launch {
            staffUserDao.getLoggedInStaffUser().collect { staffUser ->
                _staffRole.value = staffUser?.role
            }
        }
    }


    fun approveBill(approvalAction: String, reason: String, billId: Int, orderIdForRefresh: String?) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            repository.approveBill(approvalAction , reason , billId)
                .onSuccess { 
                    // Handle success, maybe clear current bill or refresh tables
                    orderIdForRefresh?.let { getBillDetails(it) }
                }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun freeTheTable(tableId: Int, cancelOrder: Boolean, reason: String) {
        viewModelScope.launch {
            _freeTableState.value = FreeTableState.Loading
            repository.freeTheTable(tableId, cancelOrder, reason)
                .onSuccess { _freeTableState.value = FreeTableState.Success(it) }
                .onFailure { _freeTableState.value = FreeTableState.Error(it.message ?: "Unknown error") }
        }
    }

    fun resetFreeTableState() {
        _freeTableState.value = FreeTableState.Idle
    }

    fun createParcelOrder(locationId: Int, customerName: String, customerPhone: String) {
        viewModelScope.launch {
            _createParcelOrderState.value = CreateParcelOrderState.Loading
            repository.createParcelOrder(locationId, customerName, customerPhone)
                .onSuccess { _createParcelOrderState.value = CreateParcelOrderState.Success(it) }
                .onFailure { _createParcelOrderState.value = CreateParcelOrderState.Error(it.message ?: "Unknown error") }
        }
    }

    fun resetCreateParcelOrderState() {
        _createParcelOrderState.value = CreateParcelOrderState.Idle
    }

    fun recordBillPayment(billId: Int, request: com.swadratna.swadratna_staff.data.remote.services.RecordPaymentRequest, orderIdForRefresh: String?) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            repository.recordBillPayment(billId, request)
                .onSuccess {
                    _paymentSuccess.emit(it)
                    orderIdForRefresh?.let { getBillDetails(it) }
                }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }
}
