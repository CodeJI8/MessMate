package com.singleminds.messmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "recurring_bills")
data class RecurringBill(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amountMinor: Long,
    val category: String = "Utilities",
    val payerId: String,
    val splitRule: String = SplitRule.EQUAL.name,
    val dayOfMonth: Int = 1,
    val isActive: Boolean = true
)
