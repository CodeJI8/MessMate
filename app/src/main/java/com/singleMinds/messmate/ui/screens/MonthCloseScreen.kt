package com.singleminds.messmate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.singleminds.messmate.data.local.entity.Month
import com.singleminds.messmate.ui.components.MessMateBackground
import com.singleminds.messmate.ui.theme.ChiliCoral
import com.singleminds.messmate.ui.theme.HerbGreen
import com.singleminds.messmate.ui.theme.Saffron

@Composable
fun MonthCloseScreen(
    currentMonth: Month?,
    availableMonths: List<Month>,
    totalSpent: Long,
    mealRate: Double,
    currencySymbol: String,
    onCloseMonth: (carryForward: Boolean) -> Unit,
    onReopenMonth: (monthId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var carryForward by remember { mutableStateOf(true) }

    MessMateBackground(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize().padding(top = 48.dp)) {
            Text(
                text = "Month Management",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (currentMonth != null) {
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(text = "Current Month: ${currentMonth.label}", style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "Total Spent", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        val totalStr = String.format("%.2f", totalSpent / 100.0)
                                        Text(text = "$currencySymbol$totalStr", style = MaterialTheme.typography.titleLarge)
                                    }
                                    Column {
                                        Text(text = "Meal Rate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        val rateStr = String.format("%.2f", mealRate)
                                        Text(text = "$currencySymbol$rateStr", style = MaterialTheme.typography.titleLarge, color = Saffron)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Carry forward net balances", style = MaterialTheme.typography.bodyMedium)
                                    Switch(
                                        checked = carryForward,
                                        onCheckedChange = { carryForward = it }
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { onCloseMonth(carryForward) },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ChiliCoral)
                                ) {
                                    Text(text = "Freeze & Close Month", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }

                item {
                    Text(text = "Month History", style = MaterialTheme.typography.titleMedium)
                }

                items(availableMonths) { month ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = month.label, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (month.isClosed) "Frozen / Closed" else "Active",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (month.isClosed) ChiliCoral else HerbGreen
                                )
                            }

                            if (month.isClosed) {
                                OutlinedButton(
                                    onClick = { onReopenMonth(month.id) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(text = "Reopen", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
