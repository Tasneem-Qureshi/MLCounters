package com.mlcounters.app.data.remote.dto

import com.mlcounters.app.data.local.entity.CounterEntity
import com.mlcounters.app.data.local.entity.CounterItemEntity
import com.mlcounters.app.data.local.entity.HeroEntity
import com.mlcounters.app.data.local.entity.ItemEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteHero(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("role") val role: String,
    @SerialName("secondaryRole") val secondaryRole: String? = null,
    @SerialName("portraitUrl") val portraitUrl: String = "",
    @SerialName("tier") val tier: String = "A"
) {
    fun toEntity(): HeroEntity = HeroEntity(
        id = id,
        name = name,
        role = role,
        secondaryRole = secondaryRole,
        portraitUrl = portraitUrl,
        tier = tier
    )
}

@Serializable
data class RemoteCounter(
    @SerialName("heroId") val heroId: Int,
    @SerialName("counterHeroId") val counterHeroId: Int,
    @SerialName("counterScore") val counterScore: Int,
    @SerialName("reason") val reason: String = ""
) {
    fun toEntity(): CounterEntity = CounterEntity(
        heroId = heroId,
        counterHeroId = counterHeroId,
        counterScore = counterScore,
        reason = reason
    )
}

@Serializable
data class RemoteItem(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String,
    @SerialName("subType") val subType: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("iconUrl") val iconUrl: String = "",
    @SerialName("stats") val stats: String = "",
    @SerialName("passive") val passive: String = ""
) {
    fun toEntity(): ItemEntity = ItemEntity(
        id = id,
        name = name,
        type = type,
        subType = subType,
        description = description,
        iconUrl = iconUrl,
        stats = stats,
        passive = passive
    )
}

@Serializable
data class RemoteCounterItem(
    @SerialName("againstHeroId") val againstHeroId: Int,
    @SerialName("itemId") val itemId: Int,
    @SerialName("priority") val priority: Int = 2,
    @SerialName("reason") val reason: String = ""
) {
    fun toEntity(): CounterItemEntity = CounterItemEntity(
        againstHeroId = againstHeroId,
        itemId = itemId,
        priority = priority,
        reason = reason
    )
}

@Serializable
data class RemoteDataManifest(
    @SerialName("version") val version: Int,
    @SerialName("lastUpdated") val lastUpdated: String = "",
    @SerialName("heroCount") val heroCount: Int = 0,
    @SerialName("dataUrl") val dataUrl: String = ""
)

@Serializable
data class RemoteDataBundle(
    @SerialName("heroes") val heroes: List<RemoteHero> = emptyList(),
    @SerialName("counters") val counters: List<RemoteCounter> = emptyList(),
    @SerialName("items") val items: List<RemoteItem> = emptyList(),
    @SerialName("counterItems") val counterItems: List<RemoteCounterItem> = emptyList()
)
