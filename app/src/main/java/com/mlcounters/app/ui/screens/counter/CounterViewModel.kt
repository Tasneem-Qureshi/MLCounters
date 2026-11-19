package com.mlcounters.app.ui.screens.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mlcounters.app.data.local.entity.HeroEntity
import com.mlcounters.app.data.repository.HeroRepository
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
class CounterViewModel @Inject constructor(
    private val heroRepository: HeroRepository
) : ViewModel() {

    val allHeroes: StateFlow<List<HeroEntity>> = heroRepository.getAllHeroes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedHero = MutableStateFlow<HeroEntity?>(null)
    val selectedHero: StateFlow<HeroEntity?> = _selectedHero.asStateFlow()

    private val _selectedRole = MutableStateFlow<String?>(null)
    val selectedRole: StateFlow<String?> = _selectedRole.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showOnlyMastered = MutableStateFlow(false)
    val showOnlyMastered: StateFlow<Boolean> = _showOnlyMastered.asStateFlow()

    val masteredHeroes: StateFlow<List<HeroEntity>> = combine(
        heroRepository.getAllHeroes(),
        heroRepository.getAllMastery()
    ) { heroes, masteries ->
        val masteredIds = masteries.filter { it.level > 0 }.map { it.heroId }.toSet()
        heroes.filter { masteredIds.contains(it.id) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            heroRepository.getAllHeroes().collect { list ->
                if (_selectedHero.value == null && list.isNotEmpty()) {
                    _selectedHero.value = list.first()
                }
            }
        }
    }

    private data class FilterState(
        val onlyMastered: Boolean,
        val role: String?,
        val query: String
    )

    private val filterState = combine(_showOnlyMastered, _selectedRole, _searchQuery) { onlyMastered, role, query ->
        FilterState(onlyMastered, role, query)
    }

    val counters: StateFlow<List<Pair<HeroEntity, Int>>> = _selectedHero.flatMapLatest { hero ->
        if (hero == null) flowOf(emptyList())
        else {
            combine(
                heroRepository.getCountersFor(hero.id),
                heroRepository.getAllHeroes(),
                masteredHeroes,
                filterState
            ) { counterEntities, heroes, masteredList, filter ->
                val heroMap = heroes.associateBy { it.id }
                val masteredIds = masteredList.map { it.id }.toSet()

                counterEntities.mapNotNull { ce ->
                    val counterHero = heroMap[ce.counterHeroId] ?: return@mapNotNull null

                    val matchesMastered = !filter.onlyMastered || masteredIds.contains(counterHero.id)
                    val matchesRole = filter.role.isNullOrBlank() || filter.role.equals("All", ignoreCase = true) ||
                            counterHero.role.equals(filter.role, ignoreCase = true) ||
                            counterHero.secondaryRole?.equals(filter.role, ignoreCase = true) == true
                    val matchesQuery = filter.query.isBlank() ||
                            counterHero.name.contains(filter.query, ignoreCase = true)

                    if (matchesMastered && matchesRole && matchesQuery) {
                        counterHero to ce.counterScore
                    } else null
                }.sortedByDescending { it.second }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val strongAgainst: StateFlow<List<Pair<HeroEntity, Int>>> = _selectedHero.flatMapLatest { hero ->
        if (hero == null) flowOf(emptyList())
        else {
            combine(
                heroRepository.getStrongAgainst(hero.id),
                heroRepository.getAllHeroes(),
                masteredHeroes,
                filterState
            ) { strongEntities, heroes, masteredList, filter ->
                val heroMap = heroes.associateBy { it.id }
                val masteredIds = masteredList.map { it.id }.toSet()

                strongEntities.mapNotNull { se ->
                    val victimHero = heroMap[se.heroId] ?: return@mapNotNull null

                    val matchesMastered = !filter.onlyMastered || masteredIds.contains(victimHero.id)
                    val matchesRole = filter.role.isNullOrBlank() || filter.role.equals("All", ignoreCase = true) ||
                            victimHero.role.equals(filter.role, ignoreCase = true) ||
                            victimHero.secondaryRole?.equals(filter.role, ignoreCase = true) == true
                    val matchesQuery = filter.query.isBlank() ||
                            victimHero.name.contains(filter.query, ignoreCase = true)

                    if (matchesMastered && matchesRole && matchesQuery) {
                        victimHero to se.counterScore
                    } else null
                }.sortedByDescending { it.second }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectHero(id: Int) {
        viewModelScope.launch {
            val hero = allHeroes.value.find { it.id == id }
            if (hero != null) {
                _selectedHero.value = hero
            }
        }
    }

    fun setRole(role: String?) {
        _selectedRole.value = if (role.equals("All", ignoreCase = true)) null else role
    }

    fun setSearch(query: String) {
        _searchQuery.value = query
    }

    fun toggleMasteredFilter() {
        _showOnlyMastered.value = !_showOnlyMastered.value
    }
}
