package com.swadratna.swadratna_staff.utils.permissions

import com.swadratna.swadratna_staff.data.local.dao.StaffUserDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManager @Inject constructor(
    private val staffUserDao: StaffUserDao
) {
    // Scope for the singleton to observe user data
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _currentUserPermissions = MutableStateFlow<Set<String>>(emptySet())
    val currentUserPermissions: StateFlow<Set<String>> = _currentUserPermissions.asStateFlow()

    init {
        scope.launch {
            staffUserDao.getLoggedInStaffUser().collectLatest { user ->
                val perms = user?.permission?.toSet() ?: emptySet()
                _currentUserPermissions.value = perms
            }
        }
    }

    // Generic check
    fun hasPermission(permission: String): Boolean {
        return _currentUserPermissions.value.contains(permission)
    }

    // --- Specific Checks ---

    // Orders
    fun canViewOrders(): Boolean = hasPermission(Permissions.ORDERS_VIEW)
    fun canUpdateOrders(): Boolean = hasPermission(Permissions.ORDERS_UPDATE)
    fun canApproveOrders(): Boolean = hasPermission(Permissions.ORDERS_APPROVE)
    fun canCreateOrders(): Boolean = hasPermission(Permissions.ORDERS_CREATE)

    // Inventory
    fun canViewInventory(): Boolean = hasPermission(Permissions.INVENTORY_VIEW)
    fun canUpdateInventory(): Boolean = hasPermission(Permissions.INVENTORY_UPDATE)

    // Staff
    fun canViewStaff(): Boolean = hasPermission(Permissions.STAFF_VIEW)
    fun canUpdateStaff(): Boolean = hasPermission(Permissions.STAFF_UPDATE)

    // Reports
    fun canViewReports(): Boolean = hasPermission(Permissions.REPORTS_VIEW)

    // Bills
    fun canApproveBills(): Boolean = hasPermission(Permissions.BILLS_APPROVE)

    // Menu
    fun canViewMenu(): Boolean = hasPermission(Permissions.MENU_VIEW)
    fun canUpdateMenu(): Boolean = hasPermission(Permissions.MENU_UPDATE)

    // Customers
    fun canViewCustomers(): Boolean = hasPermission(Permissions.CUSTOMERS_VIEW)
    fun canCreateCustomers(): Boolean = hasPermission(Permissions.CUSTOMERS_CREATE)
}
