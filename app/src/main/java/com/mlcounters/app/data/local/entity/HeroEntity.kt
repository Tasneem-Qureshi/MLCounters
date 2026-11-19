package com.mlcounters.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "heroes")
data class HeroEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val role: String,
    val secondaryRole: String? = null,
    val portraitUrl: String,
    val tier: String = "A"
)
