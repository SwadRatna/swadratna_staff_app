package com.swadratna.swadratna_staff.ui.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swadratna.swadratna_staff.data.local.dao.StaffUserDao
import com.swadratna.swadratna_staff.data.remote.model.Category
import com.swadratna.swadratna_staff.data.remote.model.MenuItem
import com.swadratna.swadratna_staff.data.remote.repositories.ApiResult
import com.swadratna.swadratna_staff.data.remote.repositories.InventoryManagementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.* // Import all from flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: InventoryManagementRepository,
    private val staffUserDao: StaffUserDao
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _menuItemsMap = MutableStateFlow<Map<String, List<MenuItem>>>(emptyMap())
    val menuItemsMap: StateFlow<Map<String, List<MenuItem>>> = _menuItemsMap.asStateFlow()

    // This will store ALL menu items from all categories
    private val _allMenuItems = MutableStateFlow<List<MenuItem>>(emptyList())

    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()


    val filteredMenuItems: StateFlow<List<MenuItem>> = combine(
        _menuItems,
        _selectedCategory,
        _searchQuery
    ) { menuItems, selectedCategory, searchQuery ->
        menuItems.filter { item ->
            (selectedCategory == null || item.categoryId == selectedCategory.id) &&
                    item.name.contains(searchQuery, ignoreCase = true)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val staffLocationId: StateFlow<Int?> = staffUserDao.getLoggedInStaffUser()
        .map { staffUser -> staffUser?.location?.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )


    init {
        viewModelScope.launch {
            staffLocationId.collect { locationId ->
                locationId?.let {
                    getMenuItems(locationId)
                }
            }
        }
    }

    fun getMenuItems(locationId: Int, searchQuery: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            val result = repository.getMenu(locationId, searchQuery)

            result.onSuccess { response ->
                _categories.value = response.categories
                _menuItemsMap.value = response.menuItems

                // Store all menu items from all categories
                val allItems = response.menuItems.values.flatten()
                _allMenuItems.value = allItems

                // Show all items initially or items from selected category
                _menuItems.value = if (_selectedCategory.value != null) {
                    _menuItemsMap.value.getOrElse(_selectedCategory.value!!.name) { allItems }
                } else {
                    allItems
                }

                // Automatically select the first category after loading
                if (_selectedCategory.value == null && response.categories.isNotEmpty()) {
                    _selectedCategory.value = response.categories.first()
                    _menuItems.value = _menuItemsMap.value.getOrElse(response.categories.first().name) { allItems }
                }

                _loading.value = false

            }.onFailure { exception ->
                _error.value = exception.message
                _loading.value = false
            }
        }
    }

    fun onCategorySelected(category: Category) {
        _selectedCategory.value = category

        // If there's no active search, filter by selected category
        if (_searchQuery.value.isEmpty()) {
            _menuItems.value = _menuItemsMap.value.getOrElse(category.name) { _allMenuItems.value }
        }
        // If there's an active search, keep the search results (global search)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query

        _menuItems.value = if (query.isEmpty()) {
            // If search query is cleared, revert to selected category
            _selectedCategory.value?.name?.let { name ->
                _menuItemsMap.value.getOrElse(name) { _allMenuItems.value }
            } ?: _allMenuItems.value
        } else {
            // Perform a global search across ALL categories
            _allMenuItems.value.filter { item ->
                item.name.contains(query, ignoreCase = true)
            }
        }
    }

    fun onAvailabilityChanged(menuItem: MenuItem, isAvailable: Boolean) {
        viewModelScope.launch {
            when (val result = repository.updateMenuItemAvailability(staffLocationId.value!!, menuItem.id, isAvailable)) {
                is ApiResult.Success -> {
                    // Update local state if API call is successful
                    _menuItems.update { currentItems ->
                        currentItems.map { item ->
                            if (item.id == menuItem.id) {
                                item.copy(isAvailable = isAvailable)
                            } else {
                                item
                            }
                        }
                    }

                    // Also update the all items list
                    _allMenuItems.update { currentItems ->
                        currentItems.map { item ->
                            if (item.id == menuItem.id) {
                                item.copy(isAvailable = isAvailable)
                            } else {
                                item
                            }
                        }
                    }

                    // Update the items in the map as well
                    _menuItemsMap.update { currentMap ->
                        currentMap.mapValues { (_, items) ->
                            items.map { item ->
                                if (item.id == menuItem.id) {
                                    item.copy(isAvailable = isAvailable)
                                } else {
                                    item
                                }
                            }
                        }
                    }
                }
                is ApiResult.Error -> {
                    println("Error updating availability: ${result.exception.message}")
                }
            }
        }
    }
}
