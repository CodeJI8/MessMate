package com.singleminds.messmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flats")
data class Flat(
    @PrimaryKey val id: String = "default_flat",
    val name: String,
    val currencySymbol: String = "$",
    val defaultAssumeEveryoneEats: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
