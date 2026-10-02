package com.singleminds.messmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val fromMemberId: String,
    val toMemberId: String,
    val amountMinor: Long,
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val monthId: String = ""
)
