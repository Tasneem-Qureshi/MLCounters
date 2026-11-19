package com.mlcounters.app.data.repository

import com.mlcounters.app.data.local.dao.CounterItemDao
import com.mlcounters.app.data.local.dao.CounterItemWithInfo
import com.mlcounters.app.data.local.dao.ItemDao
import com.mlcounters.app.data.local.entity.CounterItemEntity
import com.mlcounters.app.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepository @Inject constructor(
    private val itemDao: ItemDao,
    private val counterItemDao: CounterItemDao
) {
    fun getAllItems(): Flow<List<ItemEntity>> = itemDao.getAll()

    fun getItemById(id: Int): Flow<ItemEntity?> = itemDao.getById(id)

    fun getItemsByType(type: String): Flow<List<ItemEntity>> = itemDao.getByType(type)

    fun getCounterItemsFor(heroId: Int): Flow<List<CounterItemEntity>> = counterItemDao.getItemsAgainst(heroId)

    fun getItemsAgainstWithInfo(heroId: Int): Flow<List<CounterItemWithInfo>> =
        counterItemDao.getItemsAgainstWithInfo(heroId)

    fun searchItems(query: String): Flow<List<ItemEntity>> = itemDao.search(query)

    suspend fun insertItems(items: List<ItemEntity>) {
        itemDao.insertAll(items)
    }

    suspend fun insertCounterItems(counterItems: List<CounterItemEntity>) {
        counterItemDao.insertAll(counterItems)
    }
}
