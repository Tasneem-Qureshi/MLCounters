package com.mlcounters.app.ui.screens.heroes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mlcounters.app.data.local.entity.HeroEntity
import com.mlcounters.app.data.repository.HeroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HeroViewModel @Inject constructor(
    private val heroRepository: HeroRepository
) : ViewModel() {

    private val _selectedRole = MutableStateFlow<String?>(null)
    val selectedRole: StateFlow<String?> = _selectedRole.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val masteryMap: StateFlow<Map<Int, Int>> = heroRepository.getAllMastery()
        .combine(MutableStateFlow(Unit)) { masteries, _ ->
            masteries.associate { it.heroId to it.level }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val heroes: StateFlow<List<HeroEntity>> = combine(
        heroRepository.getAllHeroes(),
        _selectedRole,
        _searchQuery
    ) { allHeroes, role, query ->
        allHeroes.filter { hero ->
            val matchesRole = if (role.isNullOrBlank() || role.equals("All", ignoreCase = true)) {
                true
            } else {
                hero.role.equals(role, ignoreCase = true) ||
                        hero.secondaryRole?.equals(role, ignoreCase = true) == true
            }

            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                hero.name.contains(query, ignoreCase = true) ||
                        hero.role.contains(query, ignoreCase = true)
            }

            matchesRole && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setRole(role: String?) {
        _selectedRole.value = if (role.equals("All", ignoreCase = true)) null else role
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                heroRepository.refreshData()
            } catch (_: Exception) {
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
