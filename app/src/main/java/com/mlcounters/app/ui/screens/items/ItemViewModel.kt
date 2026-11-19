package com.mlcounters.app.ui.screens.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mlcounters.app.data.local.entity.CounterItemEntity
import com.mlcounters.app.data.local.entity.HeroEntity
import com.mlcounters.app.data.local.entity.ItemEntity
import com.mlcounters.app.data.repository.HeroRepository
import com.mlcounters.app.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ItemViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val heroRepository: HeroRepository
) : ViewModel() {

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedHero = MutableStateFlow<HeroEntity?>(null)
    val selectedHero: StateFlow<HeroEntity?> = _selectedHero.asStateFlow()

    val allHeroes: StateFlow<List<HeroEntity>> = heroRepository.getAllHeroes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val counterItems: StateFlow<List<CounterItemEntity>> = _selectedHero.flatMapLatest { hero ->
        if (hero == null) flowOf(emptyList())
        else itemRepository.getCounterItemsFor(hero.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val items: StateFlow<List<ItemEntity>> = combine(
        itemRepository.getAllItems(),
        _selectedType,
        _searchQuery
    ) { allItems, type, query ->
        allItems.filter { item ->
            val matchesType = type.isNullOrBlank() || type.equals("All", ignoreCase = true) ||
                    item.type.equals(type, ignoreCase = true)

            val matchesQuery = query.isBlank() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.type.contains(query, ignoreCase = true) ||
                    item.passive.contains(query, ignoreCase = true) ||
                    item.stats.contains(query, ignoreCase = true)

            matchesType && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setType(type: String?) {
        _selectedType.value = if (type.equals("All", ignoreCase = true)) null else type
    }

    fun setSearch(query: String) {
        _searchQuery.value = query
    }

    fun selectHeroForCounterItems(id: Int?) {
        viewModelScope.launch {
            if (id == null) {
                _selectedHero.value = null
            } else {
                _selectedHero.value = allHeroes.value.find { it.id == id }
            }
        }
    }
}
