package com.mlcounters.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "counter_items",
    primaryKeys = ["againstHeroId", "itemId"],
    indices = [
        Index(value = ["againstHeroId"]),
        Index(value = ["itemId"])
    ]
)
data class CounterItemEntity(
    val againstHeroId: Int,
    val itemId: Int,
    val priority: Int,
    val reason: String
) {
    val id: String get() = "${againstHeroId}_${itemId}"
}
