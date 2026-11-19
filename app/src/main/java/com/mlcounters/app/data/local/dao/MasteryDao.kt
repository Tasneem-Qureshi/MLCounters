package com.mlcounters.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mlcounters.app.data.local.entity.MasteryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MasteryDao {
    @Query("SELECT * FROM mastery ORDER BY lastUpdated DESC")
    fun getAll(): Flow<List<MasteryEntity>>

    @Query("SELECT * FROM mastery WHERE heroId = :heroId")
    fun getByHeroId(heroId: Int): Flow<MasteryEntity?>

    @Query("SELECT * FROM mastery WHERE level = :level ORDER BY lastUpdated DESC")
    fun getByLevel(level: Int): Flow<List<MasteryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(mastery: MasteryEntity)

    @Query("DELETE FROM mastery WHERE heroId = :heroId")
    suspend fun delete(heroId: Int)

    // Convenience & Compatibility methods
    fun getAllMastery(): Flow<List<MasteryEntity>> = getAll()

    @Query("SELECT heroId FROM mastery WHERE level > 0")
    fun getMasteredHeroIds(): Flow<List<Int>>

    @Query("SELECT * FROM mastery WHERE heroId = :heroId LIMIT 1")
    suspend fun getMasteryForHero(heroId: Int): MasteryEntity?

    suspend fun insertMastery(mastery: MasteryEntity) = upsert(mastery)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(masteries: List<MasteryEntity>)
}
