package com.mlcounters.app.overlay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mlcounters.app.data.local.entity.HeroEntity
import com.mlcounters.app.data.local.entity.ItemEntity
import com.mlcounters.app.data.repository.CounterRepository
import com.mlcounters.app.detection.HeroDetectionEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OverlayViewModel @Inject constructor(
    private val repository: CounterRepository,
    private val detectionEngine: HeroDetectionEngine
) : ViewModel() {

    // 1. Detected heroes from camera/screen capture
    private val _detectedHeroes = MutableStateFlow<List<HeroEntity>>(emptyList())
    val detectedHeroes: StateFlow<List<HeroEntity>> = _detectedHeroes.asStateFlow()

    // 2. Manually entered enemy heroes
    private val _manualHeroes = MutableStateFlow<List<HeroEntity>>(emptyList())
    val manualHeroes: StateFlow<List<HeroEntity>> = _manualHeroes.asStateFlow()

    // 3. Combined deduplicated list of all enemy heroes (max 5 in standard draft)
    val allEnemyHeroes: StateFlow<List<HeroEntity>> = combine(
        _detectedHeroes,
        _manualHeroes
    ) { detected, manual ->
        (detected + manual).distinctBy { it.id }.take(5)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // 4. Counter picks for each enemy hero: EnemyHeroId -> List of Pair(CounterHero, Score)
    private val _counterPicks = MutableStateFlow<Map<Int, List<Pair<HeroEntity, Int>>>>(emptyMap())
    val counterPicks: StateFlow<Map<Int, List<Pair<HeroEntity, Int>>>> = _counterPicks.asStateFlow()

    // 5. Counter items recommended against the enemy team
    private val _counterItems = MutableStateFlow<List<Pair<ItemEntity, String>>>(emptyList())
    val counterItems: StateFlow<List<Pair<ItemEntity, String>>> = _counterItems.asStateFlow()

    // 6. Selected role filter (e.g. null/"All", "Tank", "Fighter", "Assassin", "Mage", "Marksman", "Support")
    private val _selectedRole = MutableStateFlow<String?>(null)
    val selectedRole: StateFlow<String?> = _selectedRole.asStateFlow()

    // 7. Scanning state
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // 8. Expanded/Collapsed state
    private val _isExpanded = MutableStateFlow(true)
    val isExpanded: StateFlow<Boolean> = _isExpanded.asStateFlow()

    // 9. Mastered hero IDs
    private val _masteredHeroes = MutableStateFlow<Set<Int>>(emptySet())
    val masteredHeroes: StateFlow<Set<Int>> = _masteredHeroes.asStateFlow()

    // All available heroes for manual hero picker
    val allAvailableHeroes: StateFlow<List<HeroEntity>> = repository.getAllHeroes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Overlay visual settings
    private val _overlayOpacity = MutableStateFlow(0.92f)
    val overlayOpacity: StateFlow<Float> = _overlayOpacity.asStateFlow()

    private val _detectionSensitivity = MutableStateFlow(0.75f)
    val detectionSensitivity: StateFlow<Float> = _detectionSensitivity.asStateFlow()

    init {
        // Observe mastered heroes
        viewModelScope.launch {
            repository.getMasteredHeroIds().collect { ids ->
                _masteredHeroes.value = ids.toSet()
            }
        }

        // Whenever enemy heroes or selected role changes, recompute counters and items
        viewModelScope.launch {
            combine(allEnemyHeroes, _selectedRole) { enemies, role ->
                Pair(enemies, role)
            }.collect { (enemies, role) ->
                recomputeCountersAndItems(enemies, role)
            }
        }
    }

    private suspend fun recomputeCountersAndItems(enemies: List<HeroEntity>, role: String?) {
        val enemyIds = enemies.map { it.id }
        if (enemyIds.isEmpty()) {
            _counterPicks.value = emptyMap()
            _counterItems.value = emptyList()
            return
        }

        // Compute hero counters
        val picks = repository.getCountersForEnemyTeam(enemyIds, role)
        _counterPicks.value = picks

        // Compute item counters
        val items = repository.getCounterItemsForEnemyTeam(enemyIds)
        _counterItems.value = items
    }

    fun setDetectedHeroes(heroes: List<HeroEntity>) {
        _detectedHeroes.value = heroes
    }

    fun setManualHeroes(heroes: List<HeroEntity>) {
        _manualHeroes.value = heroes.take(5)
    }

    fun addManualHero(id: Int) {
        val current = _manualHeroes.value
        if (current.size >= 5 || current.any { it.id == id }) return

        viewModelScope.launch {
            val hero = repository.getHeroById(id)
            if (hero != null) {
                _manualHeroes.value = current + hero
            }
        }
    }

    fun addManualHero(hero: HeroEntity) {
        val current = _manualHeroes.value
        if (current.size >= 5 || current.any { it.id == hero.id }) return
        _manualHeroes.value = current + hero
    }

    fun removeManualHero(id: Int) {
        _manualHeroes.value = _manualHeroes.value.filter { it.id != id }
    }

    fun removeEnemyHero(id: Int) {
        _manualHeroes.value = _manualHeroes.value.filter { it.id != id }
        _detectedHeroes.value = _detectedHeroes.value.filter { it.id != id }
    }

    fun clearManual() {
        _manualHeroes.value = emptyList()
    }

    fun clearAllEnemies() {
        _manualHeroes.value = emptyList()
        _detectedHeroes.value = emptyList()
    }

    fun setRole(role: String?) {
        _selectedRole.value = if (role == "All") null else role
    }

    fun toggleExpanded() {
        _isExpanded.value = !_isExpanded.value
    }

    fun setExpanded(expanded: Boolean) {
        _isExpanded.value = expanded
    }

    fun startScan() {
        _isScanning.value = true
    }

    fun stopScan() {
        _isScanning.value = false
    }

    fun setOverlayOpacity(opacity: Float) {
        _overlayOpacity.value = opacity.coerceIn(0.2f, 1.0f)
    }

    fun setDetectionSensitivity(sensitivity: Float) {
        _detectionSensitivity.value = sensitivity.coerceIn(0.4f, 0.95f)
        detectionEngine.setConfidenceThreshold(sensitivity)
    }
}
