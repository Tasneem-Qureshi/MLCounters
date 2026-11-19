package com.mlcounters.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val type: String,
    val subType: String,
    val description: String,
    val iconUrl: String,
    val stats: String,
    val passive: String
)
