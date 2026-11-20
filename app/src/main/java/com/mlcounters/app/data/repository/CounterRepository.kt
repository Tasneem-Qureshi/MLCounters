package com.mlcounters.app.data.repository

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
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
import com.mlcounters.app.data.remote.GitHubDataApi
import com.mlcounters.app.data.remote.dto.RemoteCounter
import com.mlcounters.app.data.remote.dto.RemoteCounterItem
import com.mlcounters.app.data.remote.dto.RemoteHero
import com.mlcounters.app.data.remote.dto.RemoteItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

data class CounterResult(
    val hero: HeroEntity,
    val counterScore: Int,
    val counterReasons: List<String>,
    val heroesCountered: List<Int>
)

@Singleton
class CounterRepository @Inject constructor(
    private val database: AppDatabase,
    private val heroDao: HeroDao,
    private val counterDao: CounterDao,
    private val itemDao: ItemDao,
    private val counterItemDao: CounterItemDao,
    private val masteryDao: MasteryDao,
    private val gitHubDataApi: dagger.Lazy<GitHubDataApi>
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

    suspend fun getCounterPicks(enemyHeroIds: List<Int>): List<CounterResult> = withContext(Dispatchers.IO) {
        if (enemyHeroIds.isEmpty()) return@withContext emptyList()

        val counters = counterDao.getCountersForHeroes(enemyHeroIds)
        if (counters.isEmpty()) return@withContext emptyList()

        val counterHeroIds = counters.map { it.counterHeroId }.distinct()
        val heroes = heroDao.getHeroesByIds(counterHeroIds).associateBy { it.id }

        val scoredCounters = counters.groupBy { it.counterHeroId }
            .mapNotNull { (heroId, counterList) ->
                val hero = heroes[heroId] ?: return@mapNotNull null
                val totalScore = counterList.sumOf { it.counterScore }
                val averageScore = totalScore / counterList.size
                val reasons = counterList.map { it.reason }

                CounterResult(
                    hero = hero,
                    counterScore = averageScore,
                    counterReasons = reasons,
                    heroesCountered = counterList.map { it.heroId }
                )
            }
            .sortedByDescending { it.counterScore }

        scoredCounters
    }

    suspend fun getCountersForEnemyTeam(
        enemyIds: List<Int>,
        roleFilter: String? = null
    ): Map<Int, List<Pair<HeroEntity, Int>>> = withContext(Dispatchers.IO) {
        if (enemyIds.isEmpty()) return@withContext emptyMap()
        val result = mutableMapOf<Int, List<Pair<HeroEntity, Int>>>()
        for (enemyId in enemyIds) {
            val counters = counterDao.getTopCountersForHero(enemyId, limit = 5)
            val heroIds = counters.map { it.counterHeroId }
            val heroes = heroDao.getHeroesByIds(heroIds).associateBy { it.id }
            val pairs = counters.mapNotNull { c ->
                val h = heroes[c.counterHeroId] ?: return@mapNotNull null
                if (roleFilter.isNullOrBlank() || roleFilter.equals("All", ignoreCase = true) ||
                    h.role.equals(roleFilter, ignoreCase = true) ||
                    h.secondaryRole?.equals(roleFilter, ignoreCase = true) == true
                ) {
                    h to c.counterScore
                } else null
            }
            result[enemyId] = pairs
        }
        result
    }

    suspend fun getCounterItems(enemyHeroIds: List<Int>): List<Pair<ItemEntity, String>> = withContext(Dispatchers.IO) {
        if (enemyHeroIds.isEmpty()) return@withContext emptyList()

        val counterItems = counterItemDao.getItemCountersForHeroes(enemyHeroIds)
        if (counterItems.isEmpty()) return@withContext emptyList()

        val itemIds = counterItems.map { it.itemId }.distinct()
        val items = itemDao.getItemsByIds(itemIds)
        val itemMap = items.associateBy { it.id }

        val aggregated = mutableMapOf<Int, Pair<Int, String>>()
        for (ci in counterItems) {
            val current = aggregated[ci.itemId]
            if (current == null || ci.priority > current.first) {
                aggregated[ci.itemId] = Pair(ci.priority, ci.reason)
            }
        }

        aggregated.entries
            .sortedByDescending { it.value.first }
            .mapNotNull { (itemId, prioReason) ->
                val item = itemMap[itemId] ?: return@mapNotNull null
                Pair(item, prioReason.second)
            }
    }

    suspend fun getCounterItemsForEnemyTeam(enemyIds: List<Int>): List<Pair<ItemEntity, String>> = withContext(Dispatchers.IO) {
        getCounterItems(enemyIds)
    }

    suspend fun isDatabaseEmpty(): Boolean = withContext(Dispatchers.IO) {
        heroDao.getHeroCount() == 0
    }

    suspend fun refreshFromRemote(force: Boolean = true): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching remote data bundle from GitHub...")
            val remoteHeroes = gitHubDataApi.get().getHeroes()
            val remoteCounters = gitHubDataApi.get().getCounters()
            val remoteItems = gitHubDataApi.get().getItems()
            val remoteCounterItems = gitHubDataApi.get().getCounterItems()

            database.withTransaction {
                database.heroDao().insertAll(remoteHeroes.map { it.toEntity() })
                database.counterDao().deleteAll()
                database.counterDao().insertAll(remoteCounters.map { it.toEntity() })
                database.itemDao().insertAll(remoteItems.map { it.toEntity() })
                database.counterItemDao().deleteAll()
                database.counterItemDao().insertAll(remoteCounterItems.map { it.toEntity() })
            }
            Log.i(TAG, "Successfully refreshed remote data: ${remoteHeroes.size} heroes, ${remoteCounters.size} counters.")
            Result.success(remoteHeroes.size)
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing data from remote: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun seedInitialDataIfEmpty(context: Context) = withContext(Dispatchers.IO) {
        try {
            val currentHeroCount = heroDao.getHeroCount()
            if (currentHeroCount >= 100) {
                Log.d(TAG, "Database already initialized with $currentHeroCount heroes.")
                return@withContext
            }

            Log.i(TAG, "Seeding database with full roster from bundled assets...")
            val json = Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                isLenient = true
            }

            fun openAsset(filename: String): java.io.InputStream {
                return try {
                    context.assets.open("data/$filename")
                } catch (_: Exception) {
                    context.assets.open(filename)
                }
            }

            try {
                openAsset("heroes.json").use { stream ->
                    val text = stream.bufferedReader().use { it.readText() }
                    val heroes = json.decodeFromString<List<RemoteHero>>(text)
                    heroDao.insertAll(heroes.map { it.toEntity() })
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed seeding heroes: ${e.message}")
            }

            try {
                openAsset("counters.json").use { stream ->
                    val text = stream.bufferedReader().use { it.readText() }
                    val counters = json.decodeFromString<List<RemoteCounter>>(text)
                    counterDao.deleteAll()
                    counterDao.insertAll(counters.map { it.toEntity() })
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed seeding counters: ${e.message}")
            }

            try {
                openAsset("items.json").use { stream ->
                    val text = stream.bufferedReader().use { it.readText() }
                    val items = json.decodeFromString<List<RemoteItem>>(text)
                    itemDao.insertAll(items.map { it.toEntity() })
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed seeding items: ${e.message}")
            }

            try {
                openAsset("counter_items.json").use { stream ->
                    val text = stream.bufferedReader().use { it.readText() }
                    val counterItems = json.decodeFromString<List<RemoteCounterItem>>(text)
                    counterItemDao.deleteAll()
                    counterItemDao.insertAll(counterItems.map { it.toEntity() })
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed seeding counter items: ${e.message}")
            }

            Log.i(TAG, "Database successfully seeded from assets: ${heroDao.getHeroCount()} heroes.")
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding database: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "CounterRepository"
    }
}
