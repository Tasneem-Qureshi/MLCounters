package com.mlcounters.app.data.local.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.mlcounters.app.data.local.entity.CounterItemEntity
import com.mlcounters.app.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

data class CounterItemWithInfo(
    @Embedded
    val counterItem: CounterItemEntity,
    @Relation(
        parentColumn = "itemId",
        entityColumn = "id"
    )
    val item: ItemEntity?
)

@Dao
interface CounterItemDao {
    @Query("SELECT * FROM counter_items WHERE againstHeroId = :heroId ORDER BY priority ASC")
    fun getItemsAgainst(heroId: Int): Flow<List<CounterItemEntity>>

    @Transaction
    @Query("SELECT * FROM counter_items WHERE againstHeroId = :heroId ORDER BY priority ASC")
    fun getItemsAgainstWithInfo(heroId: Int): Flow<List<CounterItemWithInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(counterItems: List<CounterItemEntity>)

    @Query("DELETE FROM counter_items")
    suspend fun deleteAll()

    // Convenience & Compatibility methods
    @Query("SELECT * FROM counter_items WHERE againstHeroId IN (:heroIds) ORDER BY priority ASC")
    suspend fun getItemCountersForHeroes(heroIds: List<Int>): List<CounterItemEntity>

    suspend fun insertHeroItemCounters(counters: List<CounterItemEntity>) = insertAll(counters)
}
