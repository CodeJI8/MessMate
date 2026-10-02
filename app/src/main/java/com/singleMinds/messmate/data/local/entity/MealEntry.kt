package com.singleminds.messmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "meal_entries")
data class MealEntry(
    val memberId: String,
    val date: Long,
    val breakfast: Int = 0,
    val lunch: Int = 0,
    val dinner: Int = 0,
    @PrimaryKey val id: String = UUID.randomUUID().toString()
)
