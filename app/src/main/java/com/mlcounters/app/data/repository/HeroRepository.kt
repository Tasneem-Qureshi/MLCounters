package com.mlcounters.app.data.repository

import com.mlcounters.app.data.local.dao.CounterDao
import com.mlcounters.app.data.local.dao.CounterWithHero
import com.mlcounters.app.data.local.dao.HeroDao
import com.mlcounters.app.data.local.dao.MasteryDao
import com.mlcounters.app.data.local.entity.CounterEntity
import com.mlcounters.app.data.local.entity.HeroEntity
import com.mlcounters.app.data.local.entity.MasteryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeroRepository @Inject constructor(
    private val heroDao: HeroDao,
    private val counterDao: CounterDao,
    private val masteryDao: MasteryDao
) {
    fun getAllHeroes(): Flow<List<HeroEntity>> = heroDao.getAll()

    fun getHeroesByRole(role: String): Flow<List<HeroEntity>> = heroDao.getByRole(role)

    fun getHeroById(id: Int): Flow<HeroEntity?> = heroDao.getById(id)

    fun searchHeroes(query: String): Flow<List<HeroEntity>> = heroDao.search(query)

    fun getCountersFor(heroId: Int): Flow<List<CounterEntity>> = counterDao.getCountersFor(heroId)

    fun getStrongAgainst(heroId: Int): Flow<List<CounterEntity>> = counterDao.getStrongAgainst(heroId)

    fun getCounterWithHeroInfo(heroId: Int): Flow<List<CounterWithHero>> = counterDao.getCounterWithHeroInfo(heroId)

    fun getMastery(heroId: Int): Flow<MasteryEntity?> = masteryDao.getByHeroId(heroId)

    fun getAllMastery(): Flow<List<MasteryEntity>> = masteryDao.getAll()

    fun getMasteryByLevel(level: Int): Flow<List<MasteryEntity>> = masteryDao.getByLevel(level)

    suspend fun setMastery(heroId: Int, level: Int, notes: String = "") {
        val mastery = MasteryEntity(
            heroId = heroId,
            level = level,
            notes = notes,
            lastUpdated = System.currentTimeMillis()
        )
        masteryDao.upsert(mastery)
    }

    suspend fun deleteMastery(heroId: Int) {
        masteryDao.delete(heroId)
    }

    suspend fun insertHeroes(heroes: List<HeroEntity>) {
        heroDao.insertAll(heroes)
    }

    suspend fun insertCounters(counters: List<CounterEntity>) {
        counterDao.insertAll(counters)
    }
}
