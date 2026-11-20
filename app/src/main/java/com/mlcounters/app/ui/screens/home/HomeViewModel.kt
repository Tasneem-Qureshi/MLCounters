package com.mlcounters.app.ui.screens.home

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val heroRepository: HeroRepository
) : ViewModel() {

    private val _isOverlayActive = MutableStateFlow(false)
    val isOverlayActive: StateFlow<Boolean> = _isOverlayActive.asStateFlow()

    private val _dataLastUpdated = MutableStateFlow(getCurrentFormattedTime())
    val dataLastUpdated: StateFlow<String> = _dataLastUpdated.asStateFlow()

    private val _recentDetections = MutableStateFlow<List<HeroEntity>>(emptyList())
    val recentDetections: StateFlow<List<HeroEntity>> = _recentDetections.asStateFlow()

    val topCounters: StateFlow<List<Pair<HeroEntity, List<HeroEntity>>>> =
        heroRepository.getAllHeroes().combine(heroRepository.getAllHeroes()) { allHeroes, _ ->
            // Pick top heroes and resolve their top counter heroes
            val heroPairs = mutableListOf<Pair<HeroEntity, List<HeroEntity>>>()
            for (hero in allHeroes.take(5)) {
                val counterEntities = heroRepository.getCountersFor(hero.id).first()
                val counterHeroIds = counterEntities.map { it.counterHeroId }.toSet()
                val matchedCounters = allHeroes.filter { counterHeroIds.contains(it.id) }
                if (matchedCounters.isNotEmpty()) {
                    heroPairs.add(hero to matchedCounters)
                }
            }
            heroPairs
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalHeroCount: StateFlow<Int> = heroRepository.getAllHeroes()
        .combine(MutableStateFlow(0)) { heroes, _ -> heroes.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val heroes = heroRepository.getAllHeroes().first()
            if (heroes.isNotEmpty()) {
                // Populate initial recent detections with last 5 heroes
                _recentDetections.value = heroes.take(5)
            }
        }
    }

    fun toggleOverlay() {
        _isOverlayActive.value = !_isOverlayActive.value
    }

    fun refreshData() {
        viewModelScope.launch {
            try {
                heroRepository.refreshData()
            } catch (_: Exception) {}
            _dataLastUpdated.value = getCurrentFormattedTime()
            val heroes = heroRepository.getAllHeroes().first()
            if (heroes.isNotEmpty()) {
                _recentDetections.value = heroes.take(5)
            }
        }
    }

    private fun getCurrentFormattedTime(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }
}
