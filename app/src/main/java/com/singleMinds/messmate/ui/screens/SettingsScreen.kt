package com.singleminds.messmate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.singleminds.messmate.data.local.entity.AuditLog
import com.singleminds.messmate.data.local.entity.RecurringBill
import com.singleminds.messmate.ui.components.MessMateBackground
import com.singleminds.messmate.ui.theme.Saffron
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    currentCurrency: String,
    themeMode: String,
    recurringBills: List<RecurringBill>,
    auditLogs: List<AuditLog>,
    onCurrencyChanged: (String) -> Unit,
    onThemeModeChanged: (String) -> Unit,
    onExportBackup: () -> Unit,
    onImportRestore: () -> Unit,
    onShareSummaryImage: () -> Unit,
    onConfirmRecurringBill: (RecurringBill) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencies = listOf("$", "৳", "€", "₹", "£")

    MessMateBackground(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize().padding(top = 48.dp)) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Currency & Theme Card
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(text = "Preferences", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(text = "Currency Symbol", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                currencies.forEach { sym ->
                                    val isSelected = sym == currentCurrency
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) Saffron else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                            .clickable { onCurrencyChanged(sym) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = sym, style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Text(text = "Theme Mode", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("system", "light", "dark").forEach { mode ->
                                    val isSelected = mode == themeMode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) Saffron else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                            .clickable { onThemeModeChanged(mode) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = mode.replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Recurring Bills Card
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(text = "Recurring Bills", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            if (recurringBills.isEmpty()) {
                                Text(
                                    text = "No active recurring bill templates.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            } else {
                                recurringBills.forEach { bill ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(text = bill.title, style = MaterialTheme.typography.titleSmall)
                                            val amountStr = String.format("%.2f", bill.amountMinor / 100.0)
                                            Text(
                                                text = "$currentCurrency$amountStr • ${bill.category}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                        Button(
                                            onClick = { onConfirmRecurringBill(bill) },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(text = "Due This Month")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Backup & Sharing
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(text = "Backup & Sharing", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedButton(
                                onClick = onShareSummaryImage,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(text = "Share Month Summary Card")
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = onExportBackup,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(text = "Export Backup (JSON SAF)")
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = onImportRestore,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(text = "Restore Backup (JSON SAF)")
                            }
                        }
                    }
                }

                // Audit Log Timeline
                item {
                    Text(text = "Audit Log Timeline", style = MaterialTheme.typography.titleMedium)
                }

                items(auditLogs.take(10)) { log ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "${log.action} ${log.entity}", style = MaterialTheme.typography.titleSmall, color = Saffron)
                                Text(
                                    text = formatTimestamp(log.timestamp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            if (log.newValJson != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = log.newValJson,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(millis: Long): String {
    val zone = ZoneId.systemDefault()
    val dateTime = Instant.ofEpochMilli(millis).atZone(zone)
    return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))
}
