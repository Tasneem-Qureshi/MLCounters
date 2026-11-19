package com.mlcounters.app.ui.screens.heroes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mlcounters.app.data.local.entity.CounterEntity
import com.mlcounters.app.data.local.entity.CounterItemEntity
import com.mlcounters.app.data.local.entity.HeroEntity
import com.mlcounters.app.data.local.entity.ItemEntity
import com.mlcounters.app.data.local.entity.MasteryEntity
import com.mlcounters.app.data.repository.HeroRepository
import com.mlcounters.app.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HeroDetailViewModel @Inject constructor(
    private val heroRepository: HeroRepository,
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val heroId: Int = savedStateHandle.get<Int>("heroId")
        ?: savedStateHandle.get<String>("heroId")?.toIntOrNull()
        ?: 0

    val hero: StateFlow<HeroEntity?> = heroRepository.getHeroById(heroId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val counters: StateFlow<List<CounterEntity>> = heroRepository.getCountersFor(heroId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val strongAgainst: StateFlow<List<CounterEntity>> = heroRepository.getStrongAgainst(heroId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val counterItems: StateFlow<List<CounterItemEntity>> = itemRepository.getCounterItemsFor(heroId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mastery: StateFlow<MasteryEntity?> = heroRepository.getMastery(heroId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allHeroes: StateFlow<List<HeroEntity>> = heroRepository.getAllHeroes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val counterHeroes: StateFlow<List<HeroEntity>> = combine(
        counters,
        heroRepository.getAllHeroes()
    ) { counterEntities, heroes ->
        val idMap = heroes.associateBy { it.id }
        counterEntities.mapNotNull { idMap[it.counterHeroId] }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val strongAgainstHeroes: StateFlow<List<HeroEntity>> = combine(
        strongAgainst,
        heroRepository.getAllHeroes()
    ) { strongEntities, heroes ->
        val idMap = heroes.associateBy { it.id }
        strongEntities.mapNotNull { idMap[it.heroId] }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val itemsMap: StateFlow<Map<Int, ItemEntity>> = itemRepository.getAllItems()
        .combine(counterItems) { items, _ ->
            items.associateBy { it.id }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun setMastery(level: Int, notes: String = "") {
        viewModelScope.launch {
            heroRepository.setMastery(heroId, level, notes)
        }
    }
}
