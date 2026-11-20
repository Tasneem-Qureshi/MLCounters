package com.mlcounters.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "counters",
    primaryKeys = ["heroId", "counterHeroId"],
    indices = [
        Index(value = ["heroId"]),
        Index(value = ["counterHeroId"])
    ]
)
data class CounterEntity(
    val heroId: Int,
    val counterHeroId: Int,
    val counterScore: Int,
    val reason: String
) {
    val id: String get() = "${heroId}_${counterHeroId}"
}
