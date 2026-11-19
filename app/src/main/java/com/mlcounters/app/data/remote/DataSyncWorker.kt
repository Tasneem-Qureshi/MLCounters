package com.mlcounters.app.data.remote

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.work.HiltWorker
import androidx.room.withTransaction
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.mlcounters.app.data.local.AppDatabase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ml_counters_sync_prefs")
private val KEY_DATA_VERSION = intPreferencesKey("data_version")

@HiltWorker
class DataSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val gitHubDataApi: GitHubDataApi,
    private val database: AppDatabase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val manifest = gitHubDataApi.getManifest()
            val storedVersion = applicationContext.dataStore.data.map { preferences ->
                preferences[KEY_DATA_VERSION] ?: 0
            }.first()

            if (manifest.version > storedVersion) {
                val remoteHeroes = gitHubDataApi.getHeroes()
                val remoteCounters = gitHubDataApi.getCounters()
                val remoteItems = gitHubDataApi.getItems()
                val remoteCounterItems = gitHubDataApi.getCounterItems()

                val heroes = remoteHeroes.map { it.toEntity() }
                val counters = remoteCounters.map { it.toEntity() }
                val items = remoteItems.map { it.toEntity() }
                val counterItems = remoteCounterItems.map { it.toEntity() }

                database.withTransaction {
                    database.heroDao().insertAll(heroes)
                    database.counterDao().insertAll(counters)
                    database.itemDao().insertAll(items)
                    database.counterItemDao().insertAll(counterItems)
                }

                applicationContext.dataStore.edit { preferences ->
                    preferences[KEY_DATA_VERSION] = manifest.version
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "MLCounters_DataSyncWorker"

        fun enqueuePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicWorkRequest = PeriodicWorkRequestBuilder<DataSyncWorker>(
                7, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
        }

        fun enqueueOneTimeWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<DataSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "${WORK_NAME}_OneTime",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}
