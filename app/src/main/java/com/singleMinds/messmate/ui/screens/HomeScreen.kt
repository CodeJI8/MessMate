package com.singleminds.messmate.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.singleminds.messmate.data.local.entity.Member
import com.singleminds.messmate.data.local.entity.Month
import com.singleminds.messmate.engine.MemberSummary
import com.singleminds.messmate.ui.components.*
import com.singleminds.messmate.ui.theme.MemberColors
import kotlin.math.abs

@Composable
fun HomeScreen(
    flatName: String,
    monthLabel: String,
    availableMonths: List<Month>,
    mealRate: Double,
    totalSpent: Long,
    currencySymbol: String,
    members: List<Member>,
    summaryMap: Map<String, MemberSummary>,
    onSelectMonth: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var monthDropdownExpanded by remember { mutableStateOf(false) }

    MessMateBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 48.dp, bottom = 120.dp, start = 24.dp, end = 24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = flatName, style = MaterialTheme.typography.displaySmall)
                        Text(
                            text = "The Table",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Box {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.clickable { monthDropdownExpanded = true }
                        ) {
                            Text(
                                text = monthLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = monthDropdownExpanded,
                            onDismissRequest = { monthDropdownExpanded = false }
                        ) {
                            availableMonths.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.label) },
                                    onClick = {
                                        onSelectMonth(m.id)
                                        monthDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                val segments = members.map { member ->
                    val mSummary = summaryMap[member.id]
                    val spend = (mSummary?.mealCost ?: 0L) + (mSummary?.billShares ?: 0L)
                    val color = MemberColors[member.colorIndex % MemberColors.size]
                    PlateSegment(memberId = member.id.hashCode().toLong(), amount = spend, color = color)
                }

                PlateVisual(
                    mealRate = mealRate,
                    segments = segments,
                    currencySymbol = currencySymbol,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    val totalStr = String.format("%.2f", totalSpent / 100.0)
                    Text(
                        text = "$currencySymbol$totalStr",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Text(text = "Net Balances", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))

                val maxAbsBalance = members.maxOfOrNull { member ->
                    val net = summaryMap[member.id]?.netBalance ?: 0L
                    abs(net)
                } ?: 100L

                LedgerCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                        if (members.isEmpty()) {
                            Text(
                                text = "No members added yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        } else {
                            members.forEach { member ->
                                val summary = summaryMap[member.id]
                                val net = summary?.netBalance ?: 0L
                                val color = MemberColors[member.colorIndex % MemberColors.size]

                                TugBarBalance(
                                    memberName = member.name,
                                    memberColor = color,
                                    balanceMinor = net,
                                    maxAbsBalanceMinor = maxAbsBalance,
                                    currencySymbol = currencySymbol
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
