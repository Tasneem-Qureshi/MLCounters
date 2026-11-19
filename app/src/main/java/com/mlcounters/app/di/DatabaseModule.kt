package com.mlcounters.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mlcounters.app.data.local.AppDatabase
import com.mlcounters.app.data.local.dao.CounterDao
import com.mlcounters.app.data.local.dao.CounterItemDao
import com.mlcounters.app.data.local.dao.HeroDao
import com.mlcounters.app.data.local.dao.ItemDao
import com.mlcounters.app.data.local.dao.MasteryDao
import com.mlcounters.app.data.remote.dto.RemoteCounter
import com.mlcounters.app.data.remote.dto.RemoteCounterItem
import com.mlcounters.app.data.remote.dto.RemoteDataBundle
import com.mlcounters.app.data.remote.dto.RemoteHero
import com.mlcounters.app.data.remote.dto.RemoteItem
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        lateinit var database: AppDatabase

        val callback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        loadInitialData(context, database)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addCallback(callback)
            .fallbackToDestructiveMigration()
            .build()

        return database
    }

    private suspend fun loadInitialData(context: Context, database: AppDatabase) {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }

        // 1. Try reading initial_data.json bundle if present
        try {
            context.assets.open("initial_data.json").use { stream ->
                val text = stream.bufferedReader().use { it.readText() }
                val bundle = json.decodeFromString<RemoteDataBundle>(text)
                if (bundle.heroes.isNotEmpty()) {
                    database.heroDao().insertAll(bundle.heroes.map { it.toEntity() })
                }
                if (bundle.counters.isNotEmpty()) {
                    database.counterDao().insertAll(bundle.counters.map { it.toEntity() })
                }
                if (bundle.items.isNotEmpty()) {
                    database.itemDao().insertAll(bundle.items.map { it.toEntity() })
                }
                if (bundle.counterItems.isNotEmpty()) {
                    database.counterItemDao().insertAll(bundle.counterItems.map { it.toEntity() })
                }
                return
            }
        } catch (_: Exception) {}

        // 2. Fallback to individual asset files
        try {
            context.assets.open("heroes.json").use { stream ->
                val text = stream.bufferedReader().use { it.readText() }
                val heroes = json.decodeFromString<List<RemoteHero>>(text)
                database.heroDao().insertAll(heroes.map { it.toEntity() })
            }
        } catch (_: Exception) {}

        try {
            context.assets.open("counters.json").use { stream ->
                val text = stream.bufferedReader().use { it.readText() }
                val counters = json.decodeFromString<List<RemoteCounter>>(text)
                database.counterDao().insertAll(counters.map { it.toEntity() })
            }
        } catch (_: Exception) {}

        try {
            context.assets.open("items.json").use { stream ->
                val text = stream.bufferedReader().use { it.readText() }
                val items = json.decodeFromString<List<RemoteItem>>(text)
                database.itemDao().insertAll(items.map { it.toEntity() })
            }
        } catch (_: Exception) {}

        try {
            context.assets.open("counter_items.json").use { stream ->
                val text = stream.bufferedReader().use { it.readText() }
                val counterItems = json.decodeFromString<List<RemoteCounterItem>>(text)
                database.counterItemDao().insertAll(counterItems.map { it.toEntity() })
            }
        } catch (_: Exception) {}
    }

    @Provides
    fun provideHeroDao(database: AppDatabase): HeroDao = database.heroDao()

    @Provides
    fun provideCounterDao(database: AppDatabase): CounterDao = database.counterDao()

    @Provides
    fun provideItemDao(database: AppDatabase): ItemDao = database.itemDao()

    @Provides
    fun provideCounterItemDao(database: AppDatabase): CounterItemDao = database.counterItemDao()

    @Provides
    fun provideMasteryDao(database: AppDatabase): MasteryDao = database.masteryDao()
}
