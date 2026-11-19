package com.mlcounters.app.data.repository

import android.content.Context
import android.util.Log
import com.mlcounters.app.data.local.AppDatabase
import com.mlcounters.app.data.local.dao.CounterDao
import com.mlcounters.app.data.local.dao.CounterItemDao
import com.mlcounters.app.data.local.dao.HeroDao
import com.mlcounters.app.data.local.dao.ItemDao
import com.mlcounters.app.data.local.dao.MasteryDao
import com.mlcounters.app.data.local.entity.CounterEntity
import com.mlcounters.app.data.local.entity.CounterItemEntity
import com.mlcounters.app.data.local.entity.HeroEntity
import com.mlcounters.app.data.local.entity.ItemEntity
import com.mlcounters.app.data.local.entity.MasteryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CounterRepository @Inject constructor(
    private val database: AppDatabase,
    private val heroDao: HeroDao,
    private val counterDao: CounterDao,
    private val itemDao: ItemDao,
    private val counterItemDao: CounterItemDao,
    private val masteryDao: MasteryDao
) {

    fun getAllHeroes(): Flow<List<HeroEntity>> = heroDao.getAll()

    fun getMasteredHeroIds(): Flow<List<Int>> = masteryDao.getMasteredHeroIds()

    suspend fun getHeroById(id: Int): HeroEntity? = withContext(Dispatchers.IO) {
        heroDao.getHeroById(id)
    }

    suspend fun getHeroesByIds(ids: List<Int>): List<HeroEntity> = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) emptyList() else heroDao.getHeroesByIds(ids)
    }

    suspend fun searchHeroes(query: String): List<HeroEntity> = withContext(Dispatchers.IO) {
        if (query.isBlank()) emptyList() else heroDao.searchHeroes(query)
    }

    suspend fun getTopCountersForHero(
        heroId: Int,
        roleFilter: String? = null,
        limit: Int = 3
    ): List<Pair<HeroEntity, Int>> = withContext(Dispatchers.IO) {
        val counterList = counterDao.getTopCountersForHero(heroId, limit = 20)
        if (counterList.isEmpty()) return@withContext emptyList()

        val counterHeroIds = counterList.map { it.counterHeroId }
        val heroes = heroDao.getHeroesByIds(counterHeroIds).associateBy { it.id }

        counterList
            .mapNotNull { counter ->
                val hero = heroes[counter.counterHeroId] ?: return@mapNotNull null
                if (roleFilter != null && roleFilter != "All" &&
                    hero.role != roleFilter && hero.secondaryRole != roleFilter
                ) {
                    null
                } else {
                    Pair(hero, counter.counterScore)
                }
            }
            .take(limit)
    }

    suspend fun getCountersForEnemyTeam(
        enemyIds: List<Int>,
        roleFilter: String? = null
    ): Map<Int, List<Pair<HeroEntity, Int>>> = withContext(Dispatchers.IO) {
        val result = mutableMapOf<Int, List<Pair<HeroEntity, Int>>>()
        for (heroId in enemyIds) {
            result[heroId] = getTopCountersForHero(heroId, roleFilter, limit = 3)
        }
        result
    }

    suspend fun getCounterItemsForEnemyTeam(
        enemyIds: List<Int>
    ): List<Pair<ItemEntity, String>> = withContext(Dispatchers.IO) {
        if (enemyIds.isEmpty()) return@withContext emptyList()

        val itemCounters = counterItemDao.getItemCountersForHeroes(enemyIds)
        if (itemCounters.isEmpty()) return@withContext emptyList()

        val itemIds = itemCounters.map { it.itemId }.distinct()
        val itemMap = itemDao.getItemsByIds(itemIds).associateBy { it.id }

        val aggregated = mutableMapOf<Int, Pair<Int, String>>()
        for (ic in itemCounters) {
            val current = aggregated[ic.itemId]
            if (current == null || ic.priority > current.first) {
                aggregated[ic.itemId] = Pair(ic.priority, ic.reason)
            }
        }

        aggregated.entries
            .sortedByDescending { it.value.first }
            .mapNotNull { (itemId, prioReason) ->
                val item = itemMap[itemId] ?: return@mapNotNull null
                Pair(item, prioReason.second)
            }
    }

    suspend fun isDatabaseEmpty(): Boolean = withContext(Dispatchers.IO) {
        heroDao.getHeroCount() == 0
    }

    suspend fun seedInitialDataIfEmpty(context: Context) = withContext(Dispatchers.IO) {
        try {
            if (!isDatabaseEmpty()) {
                Log.d(TAG, "Database already initialized.")
                return@withContext
            }

            Log.d(TAG, "Seeding database with default heroes and counters...")
            seedDefaultHeroes()
            seedDefaultItems()
            seedDefaultCounters()
            seedDefaultMastery()
            Log.d(TAG, "Database successfully seeded.")
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding database: ${e.message}", e)
        }
    }

    private suspend fun seedDefaultHeroes() {
        val heroes = listOf(
            HeroEntity(1, "Miya", "Marksman", null, "", "A"),
            HeroEntity(2, "Balmond", "Fighter", null, "", "B"),
            HeroEntity(3, "Saber", "Assassin", null, "", "A"),
            HeroEntity(4, "Alice", "Mage", "Tank", "", "A"),
            HeroEntity(5, "Nana", "Mage", "Support", "", "S"),
            HeroEntity(6, "Tigreal", "Tank", "Support", "", "S"),
            HeroEntity(7, "Alucard", "Fighter", "Assassin", "", "B"),
            HeroEntity(8, "Karina", "Assassin", null, "", "A"),
            HeroEntity(9, "Akai", "Tank", "Support", "", "A"),
            HeroEntity(10, "Franco", "Tank", "Support", "", "S"),
            HeroEntity(17, "Fanny", "Assassin", null, "", "S"),
            HeroEntity(20, "Lolita", "Support", "Tank", "", "S"),
            HeroEntity(24, "Chou", "Fighter", null, "", "S"),
            HeroEntity(27, "Ruby", "Fighter", "Tank", "", "S"),
            HeroEntity(29, "Moskov", "Marksman", null, "", "S"),
            HeroEntity(38, "Karrie", "Marksman", null, "", "S"),
            HeroEntity(44, "Lancelot", "Assassin", null, "", "S"),
            HeroEntity(45, "Diggie", "Support", null, "", "S"),
            HeroEntity(50, "Lesley", "Marksman", "Assassin", "", "A"),
            HeroEntity(53, "Gusion", "Assassin", "Mage", "", "S"),
            HeroEntity(56, "Uranus", "Tank", null, "", "A"),
            HeroEntity(71, "Minsitthar", "Fighter", "Support", "", "A"),
            HeroEntity(75, "Khufra", "Tank", "Support", "", "S"),
            HeroEntity(78, "Esmeralda", "Mage", "Tank", "", "A"),
            HeroEntity(80, "Ling", "Assassin", null, "", "S"),
            HeroEntity(81, "Dyrroth", "Fighter", null, "", "A"),
            HeroEntity(85, "Baxia", "Tank", null, "", "A"),
            HeroEntity(87, "Wanwan", "Marksman", null, "", "S"),
            HeroEntity(93, "Yu Zhong", "Fighter", null, "", "S"),
            HeroEntity(104, "Phoveus", "Fighter", null, "", "S"),
            HeroEntity(115, "Fredrinn", "Tank", "Fighter", "", "S")
        )
        heroDao.insertAll(heroes)
    }

    private suspend fun seedDefaultItems() {
        val items = listOf(
            ItemEntity(1, "Sea Halberd", "Attack", "Physical", "Reduces shield & HP regen by 50%", "", "+80 Phys Atk, +20% Atk Spd", "Lifebane"),
            ItemEntity(2, "Glowing Wand", "Magic", "Magic", "Causes burn & reduces shield/HP regen by 50%", "", "+75 Magic Power, +400 HP", "Scorch & Lifebane"),
            ItemEntity(3, "Dominance Ice", "Defense", "Physical Defense", "Reduces nearby enemy attack speed & HP regen by 50%", "", "+500 Mana, +70 Phys Def", "Arctic Cold & Lifebane"),
            ItemEntity(4, "Antique Cuirass", "Defense", "Physical Defense", "Reduces physical attack of enemy skill casters", "", "+920 HP, +54 Phys Def", "Deter"),
            ItemEntity(5, "Blade Armor", "Defense", "Physical Defense", "Reflects basic attack damage and reduces critical damage", "", "+90 Phys Def", "Bladed Armor"),
            ItemEntity(6, "Athena's Shield", "Defense", "Magic Defense", "Grants 25% magic damage reduction shield against burst", "", "+900 HP, +62 Magic Def", "Shield"),
            ItemEntity(7, "Radiant Armor", "Defense", "Magic Defense", "Continuous magic damage reduction stacking up to 6 times", "", "+950 HP, +52 Magic Def", "Holy Blessing"),
            ItemEntity(8, "Demon Hunter Sword", "Attack", "Physical", "Deals % of enemy current HP as extra damage", "", "+35 Phys Atk, +25% Atk Spd", "Devour"),
            ItemEntity(9, "Malefic Roar", "Attack", "Physical", "Physical penetration scaling with enemy physical defense", "", "+60 Phys Atk", "Armor Buster"),
            ItemEntity(13, "Wind of Nature", "Attack", "Physical", "Grants physical damage immunity for 2s", "", "+30 Phys Atk, +20% Atk Spd", "Wind Chant")
        )
        itemDao.insertAll(items)

        val itemCounters = listOf(
            CounterItemEntity(againstHeroId = 56, itemId = 3, priority = 10, reason = "Anti-Regen: Dominance Ice counters Uranus passive healing"),
            CounterItemEntity(againstHeroId = 56, itemId = 1, priority = 9, reason = "Anti-Regen: Physical heroes build Sea Halberd against Uranus"),
            CounterItemEntity(againstHeroId = 78, itemId = 3, priority = 10, reason = "Anti-Shield: Dominance Ice counters Esmeralda shield generation"),
            CounterItemEntity(againstHeroId = 17, itemId = 4, priority = 10, reason = "Skill Defense: Antique Cuirass mitigates Fanny cable dives"),
            CounterItemEntity(againstHeroId = 17, itemId = 13, priority = 9, reason = "Immunity: Wind of Nature survives Fanny burst damage"),
            CounterItemEntity(againstHeroId = 80, itemId = 13, priority = 10, reason = "Immunity: Wind of Nature survives Ling tempest burst"),
            CounterItemEntity(againstHeroId = 53, itemId = 6, priority = 10, reason = "Magic Shield: Athena's Shield survives Gusion dagger combo"),
            CounterItemEntity(againstHeroId = 6, itemId = 8, priority = 10, reason = "DHS deals percentage HP damage to Tigreal massive health pool")
        )
        counterItemDao.insertAll(itemCounters)
    }

    private suspend fun seedDefaultCounters() {
        val counters = listOf(
            CounterEntity(heroId = 1, counterHeroId = 3, counterScore = 92, reason = "Saber point-and-click knockup bursts Miya before she ults"),
            CounterEntity(heroId = 1, counterHeroId = 24, counterScore = 88, reason = "Chou kicks Miya out of safety"),
            CounterEntity(heroId = 1, counterHeroId = 10, counterScore = 85, reason = "Franco suppress hook prevents Miya escape"),

            CounterEntity(heroId = 17, counterHeroId = 75, counterScore = 98, reason = "Khufra bouncing ball immediately stops Fanny cables"),
            CounterEntity(heroId = 17, counterHeroId = 24, counterScore = 94, reason = "Chou instant ultimate intercepts Fanny in flight"),
            CounterEntity(heroId = 17, counterHeroId = 29, counterScore = 90, reason = "Moskov pins Fanny to walls during cables"),

            CounterEntity(heroId = 78, counterHeroId = 85, counterScore = 95, reason = "Baxia anti-heal passive neuters Esmeralda shield gain"),
            CounterEntity(heroId = 78, counterHeroId = 81, counterScore = 93, reason = "Dyrroth melts Esmeralda defense with armor break"),
            CounterEntity(heroId = 78, counterHeroId = 38, counterScore = 89, reason = "Karrie true damage pierces Esmeralda shields"),

            CounterEntity(heroId = 80, counterHeroId = 71, counterScore = 97, reason = "Minsitthar ultimate grounds Ling preventing wall leaps"),
            CounterEntity(heroId = 80, counterHeroId = 75, counterScore = 95, reason = "Khufra intercepts Ling wall jumps with bouncing ball"),
            CounterEntity(heroId = 80, counterHeroId = 27, counterScore = 89, reason = "Ruby chained crowd control locks Ling down"),

            CounterEntity(heroId = 24, counterHeroId = 45, counterScore = 96, reason = "Diggie ultimate cleanses Chou crowd control combo"),
            CounterEntity(heroId = 24, counterHeroId = 104, counterScore = 91, reason = "Phoveus repeatedly smashes onto Chou dashes"),
            CounterEntity(heroId = 24, counterHeroId = 115, counterScore = 87, reason = "Fredrinn out-sustains and taunts Chou"),

            CounterEntity(heroId = 6, counterHeroId = 45, counterScore = 99, reason = "Diggie cleanses Tigreal ultimate instantly"),
            CounterEntity(heroId = 6, counterHeroId = 9, counterScore = 88, reason = "Akai spin knocks Tigreal away from team"),

            CounterEntity(heroId = 87, counterHeroId = 104, counterScore = 95, reason = "Phoveus triggers non-stop jumps on Wanwan hops"),
            CounterEntity(heroId = 87, counterHeroId = 20, counterScore = 92, reason = "Lolita shield completely blocks Wanwan crossbow darts"),
            CounterEntity(heroId = 87, counterHeroId = 75, counterScore = 88, reason = "Khufra bounces stop Wanwan movement")
        )
        counterDao.insertAll(counters)
    }

    private suspend fun seedDefaultMastery() {
        val masteries = listOf(
            MasteryEntity(heroId = 3, level = 5, notes = "Main Assassin"),
            MasteryEntity(heroId = 24, level = 4, notes = "Comfort pick Fighter"),
            MasteryEntity(heroId = 75, level = 5, notes = "Tank specialist")
        )
        masteryDao.insertAll(masteries)
    }

    companion object {
        private const val TAG = "CounterRepository"
    }
}
