package com.mlcounters.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "counter_items",
    indices = [
        Index(value = ["againstHeroId"])
    ]
)
data class CounterItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val againstHeroId: Int,
    val itemId: Int,
    val priority: Int,
    val reason: String
)
