package com.mlcounters.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "counters",
    indices = [
        Index(value = ["heroId"]),
        Index(value = ["counterHeroId"])
    ]
)
data class CounterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val heroId: Int,
    val counterHeroId: Int,
    val counterScore: Int,
    val reason: String
)
