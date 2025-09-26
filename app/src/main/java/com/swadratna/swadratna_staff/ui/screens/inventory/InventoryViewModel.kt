package com.swadratna.swadratna_staff.ui.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val repository: InventoryManagementRepository
) : ViewModel() {

    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    private val _searchQuery = MutableStateFlow("")

    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredMenuItems: StateFlow<List<MenuItem>> = combine(
        _menuItems,
        _selectedCategory,
        _searchQuery
    ) { menuItems, selectedCategory, searchQuery ->
        menuItems.filter {
            (selectedCategory == null || it.category.categoryId == selectedCategory.categoryId) &&
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val LOCATION_ID = 1 // Placeholder for location ID

    init {
        fetchMenuItems()
    }

    private fun fetchMenuItems() {
        viewModelScope.launch {
            when (val result = repository.getMenuByLocation(LOCATION_ID)) {
                is ApiResult.Success -> {
                    _menuItems.value = result.data
                    val uniqueCategories = result.data.map { it.category }.distinctBy { it.categoryId }
                    _categories.value = uniqueCategories
                    if (_selectedCategory.value == null && uniqueCategories.isNotEmpty()) {
                        _selectedCategory.value = uniqueCategories.first()
                    }
                }
                is ApiResult.Error -> {
                    // Handle error, e.g., log it or show a toast
                    println("Error fetching menu items: ${result.exception.message}")
                }
            }
        }
    }

    fun onCategorySelected(category: Category) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onAvailabilityChanged(menuItem: MenuItem, isAvailable: Boolean) {
        viewModelScope.launch {
            when (val result = repository.updateMenuItemAvailability(LOCATION_ID, menuItem.id, isAvailable)) {
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
                }
                is ApiResult.Error -> {
                    // Handle error, e.g., log it or show a toast
                    println("Error updating availability: ${result.exception.message}")
                }
            }
        }
    }
}
