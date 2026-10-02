package com.singleminds.messmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amountMinor: Long,
    val date: Long,
    val payerId: String,
    val type: String = ExpenseType.MEAL_POOL.name,
    val category: String = "General",
    val splitRule: String = SplitRule.EQUAL.name,
    val note: String = "",
    val receiptImagePath: String? = null,
    val monthId: String = ""
)
