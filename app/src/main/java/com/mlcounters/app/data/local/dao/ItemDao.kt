package com.mlcounters.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mlcounters.app.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY name ASC")
    fun getAll(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id")
    fun getById(id: Int): Flow<ItemEntity?>

    @Query("SELECT * FROM items WHERE type = :type ORDER BY name ASC")
    fun getByType(type: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemEntity>)

    @Query("DELETE FROM items")
    suspend fun deleteAll()

    // Convenience & Compatibility methods
    fun getAllItems(): Flow<List<ItemEntity>> = getAll()

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Int): ItemEntity?

    @Query("SELECT * FROM items WHERE id IN (:ids)")
    suspend fun getItemsByIds(ids: List<Int>): List<ItemEntity>

    @Query("SELECT COUNT(*) FROM items")
    suspend fun getItemCount(): Int

    suspend fun insertItems(items: List<ItemEntity>) = insertAll(items)
}
