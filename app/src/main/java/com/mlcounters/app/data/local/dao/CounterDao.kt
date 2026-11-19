package com.mlcounters.app.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.mlcounters.app.data.local.entity.CounterEntity
import com.mlcounters.app.data.local.entity.HeroEntity
import kotlinx.coroutines.flow.Flow

data class CounterWithHero(
    @Embedded
    val counter: CounterEntity,
    @Relation(
        parentColumn = "counterHeroId",
        entityColumn = "id"
    )
    val counterHero: HeroEntity?
)

@Dao
interface CounterDao {
    @Query("SELECT * FROM counters WHERE heroId = :heroId ORDER BY counterScore DESC")
    fun getCountersFor(heroId: Int): Flow<List<CounterEntity>>

    @Query("SELECT * FROM counters WHERE counterHeroId = :heroId ORDER BY counterScore DESC")
    fun getStrongAgainst(heroId: Int): Flow<List<CounterEntity>>

    @Transaction
    @Query("SELECT * FROM counters WHERE heroId = :heroId ORDER BY counterScore DESC")
    fun getCounterWithHeroInfo(heroId: Int): Flow<List<CounterWithHero>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(counters: List<CounterEntity>)

    @Query("DELETE FROM counters")
    suspend fun deleteAll()

    // Convenience & Compatibility methods
    fun getCountersForHero(heroId: Int): Flow<List<CounterEntity>> = getCountersFor(heroId)

    @Query("SELECT * FROM counters WHERE heroId = :heroId ORDER BY counterScore DESC LIMIT :limit")
    suspend fun getTopCountersForHero(heroId: Int, limit: Int = 3): List<CounterEntity>

    @Query("SELECT * FROM counters WHERE heroId IN (:heroIds) ORDER BY counterScore DESC")
    suspend fun getCountersForHeroes(heroIds: List<Int>): List<CounterEntity>

    @Query("SELECT COUNT(*) FROM counters")
    suspend fun getCounterCount(): Int
}
