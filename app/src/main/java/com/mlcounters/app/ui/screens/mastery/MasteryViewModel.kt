package com.mlcounters.app.ui.screens.mastery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mlcounters.app.data.local.entity.HeroEntity
import com.mlcounters.app.data.local.entity.MasteryEntity
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

data class MasteryStats(
    val notPlayed: Int = 0,
    val beginner: Int = 0,
    val intermediate: Int = 0,
    val expert: Int = 0,
    val master: Int = 0
)

@HiltViewModel
class MasteryViewModel @Inject constructor(
    private val heroRepository: HeroRepository
) : ViewModel() {

    val allHeroes: StateFlow<List<HeroEntity>> = heroRepository.getAllHeroes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val masteryMap: StateFlow<Map<Int, MasteryEntity>> = heroRepository.getAllMastery()
        .combine(MutableStateFlow(Unit)) { masteries, _ ->
            masteries.associateBy { it.heroId }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _selectedLevel = MutableStateFlow<Int?>(null)
    val selectedLevel: StateFlow<Int?> = _selectedLevel.asStateFlow()

    val stats: StateFlow<MasteryStats> = combine(
        allHeroes,
        masteryMap
    ) { heroes, map ->
        var notPlayed = 0
        var beginner = 0
        var intermediate = 0
        var expert = 0
        var master = 0

        heroes.forEach { hero ->
            val level = map[hero.id]?.level ?: 0
            when (level) {
                1 -> beginner++
                2 -> intermediate++
                3 -> expert++
                4 -> master++
                else -> notPlayed++
            }
        }

        MasteryStats(
            notPlayed = notPlayed,
            beginner = beginner,
            intermediate = intermediate,
            expert = expert,
            master = master
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MasteryStats())

    val filteredHeroes: StateFlow<List<HeroEntity>> = combine(
        allHeroes,
        masteryMap,
        _selectedLevel
    ) { heroes, map, filterLvl ->
        if (filterLvl == null) {
            heroes
        } else {
            heroes.filter { hero ->
                val level = map[hero.id]?.level ?: 0
                level == filterLvl
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setMastery(heroId: Int, level: Int, notes: String) {
        viewModelScope.launch {
            heroRepository.setMastery(heroId, level, notes)
        }
    }

    fun setLevelFilter(level: Int?) {
        _selectedLevel.value = level
    }
}
