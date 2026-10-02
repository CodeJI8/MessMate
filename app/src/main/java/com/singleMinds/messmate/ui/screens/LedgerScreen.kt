package com.singleminds.messmate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.singleminds.messmate.data.local.entity.Expense
import com.singleminds.messmate.data.local.entity.Member
import com.singleminds.messmate.ui.components.EmptyStateIllustration
import com.singleminds.messmate.ui.components.MessMateBackground
import com.singleminds.messmate.ui.theme.ChiliCoral
import com.singleminds.messmate.ui.theme.HerbGreen
import com.singleminds.messmate.ui.theme.MemberColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun LedgerScreen(
    expenses: List<Expense>,
    members: List<Member>,
    currencySymbol: String,
    onDeleteExpense: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    MessMateBackground(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize().padding(top = 48.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Ledger", style = MaterialTheme.typography.displaySmall)
                Text(
                    text = "${expenses.size} items",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (expenses.isEmpty()) {
                EmptyStateIllustration(
                    title = "No Expenses Yet",
                    subtitle = "Tap the '+' button at the bottom to log your first expense."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(expenses) { expense ->
                        val payer = members.find { it.id == expense.payerId }
                        val payerColor = if (payer != null) MemberColors[payer.colorIndex % MemberColors.size] else MaterialTheme.colorScheme.primary

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(payerColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = payer?.name?.take(1) ?: "P",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = payerColor
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = expense.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${payer?.name ?: "Unknown"} • ${expense.category}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (expense.type == "MEAL_POOL") HerbGreen.copy(alpha = 0.15f) else ChiliCoral.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (expense.type == "MEAL_POOL") "Meal Pool" else "Fixed Bill",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (expense.type == "MEAL_POOL") HerbGreen else ChiliCoral,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    val amountStr = String.format("%.2f", expense.amountMinor / 100.0)
                                    Text(
                                        text = "$currencySymbol$amountStr",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = formatDate(expense.date),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Delete",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.clickable { expenseToDelete = expense }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Delete Expense?") },
            text = { Text("Are you sure you want to delete '${expenseToDelete?.title}'? This action will be logged in Audit Log.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        expenseToDelete?.let { onDeleteExpense(it) }
                        expenseToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatDate(millis: Long): String {
    val zone = ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
    return date.format(DateTimeFormatter.ofPattern("MMM dd"))
}
