package com.singleminds.messmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "months")
data class Month(
    @PrimaryKey val id: String, // e.g. "2025-10"
    val label: String, // e.g. "October 2025"
    val startTimestamp: Long,
    val endTimestamp: Long,
    val isClosed: Boolean = false,
    val carryForward: Boolean = false
)
