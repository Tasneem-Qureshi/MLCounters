package com.mlcounters.app.data.remote

import com.mlcounters.app.data.remote.dto.RemoteCounter
import com.mlcounters.app.data.remote.dto.RemoteCounterItem
import com.mlcounters.app.data.remote.dto.RemoteDataManifest
import com.mlcounters.app.data.remote.dto.RemoteHero
import com.mlcounters.app.data.remote.dto.RemoteItem
import retrofit2.http.GET

interface GitHubDataApi {
    @GET("manifest.json")
    suspend fun getManifest(): RemoteDataManifest

    @GET("heroes.json")
    suspend fun getHeroes(): List<RemoteHero>

    @GET("counters.json")
    suspend fun getCounters(): List<RemoteCounter>

    @GET("items.json")
    suspend fun getItems(): List<RemoteItem>

    @GET("counter_items.json")
    suspend fun getCounterItems(): List<RemoteCounterItem>
}
