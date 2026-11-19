package com.mlcounters.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
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

@Database(
    entities = [
        HeroEntity::class,
        CounterEntity::class,
        ItemEntity::class,
        CounterItemEntity::class,
        MasteryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun heroDao(): HeroDao
    abstract fun counterDao(): CounterDao
    abstract fun itemDao(): ItemDao
    abstract fun counterItemDao(): CounterItemDao
    abstract fun masteryDao(): MasteryDao

    companion object {
        const val DATABASE_NAME = "ml_counters.db"
    }
}
