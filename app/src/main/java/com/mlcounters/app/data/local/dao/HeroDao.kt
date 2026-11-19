package com.mlcounters.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mlcounters.app.data.local.entity.HeroEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeroDao {
    @Query("SELECT * FROM heroes ORDER BY name ASC")
    fun getAll(): Flow<List<HeroEntity>>

    @Query("SELECT * FROM heroes WHERE role = :role OR secondaryRole = :role ORDER BY name ASC")
    fun getByRole(role: String): Flow<List<HeroEntity>>

    @Query("SELECT * FROM heroes WHERE id = :id")
    fun getById(id: Int): Flow<HeroEntity?>

    @Query("SELECT * FROM heroes WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<HeroEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(heroes: List<HeroEntity>)

    @Query("DELETE FROM heroes")
    suspend fun deleteAll()

    // Convenience & Compatibility methods
    fun getAllHeroes(): Flow<List<HeroEntity>> = getAll()

    @Query("SELECT * FROM heroes WHERE id = :id LIMIT 1")
    suspend fun getHeroById(id: Int): HeroEntity?

    @Query("SELECT * FROM heroes WHERE id IN (:ids)")
    suspend fun getHeroesByIds(ids: List<Int>): List<HeroEntity>

    @Query("SELECT COUNT(*) FROM heroes")
    suspend fun getHeroCount(): Int

    @Query("SELECT * FROM heroes WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchHeroes(query: String): List<HeroEntity>
}
