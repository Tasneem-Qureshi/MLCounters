package com.mlcounters.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mastery")
data class MasteryEntity(
    @PrimaryKey
    val heroId: Int,
    val level: Int,
    val notes: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)
