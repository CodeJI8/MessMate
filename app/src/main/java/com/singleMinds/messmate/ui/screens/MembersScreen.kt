package com.singleminds.messmate.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.unit.dp
import com.singleminds.messmate.data.local.entity.Member
import com.singleminds.messmate.engine.MemberSummary
import com.singleminds.messmate.ui.components.MessMateBackground
import com.singleminds.messmate.ui.theme.ChiliCoral
import com.singleminds.messmate.ui.theme.HerbGreen
import com.singleminds.messmate.ui.theme.MemberColors
import com.singleminds.messmate.ui.theme.Saffron
import kotlin.math.abs

@Composable
fun MembersScreen(
    members: List<Member>,
    summaryMap: Map<String, MemberSummary>,
    currencySymbol: String,
    onAddMember: (name: String, colorIndex: Int) -> Unit,
    onUpdateMember: (Member) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var expandedMemberId by remember { mutableStateOf<String?>(null) }

    MessMateBackground(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize().padding(top = 48.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Members", style = MaterialTheme.typography.displaySmall)
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(text = "+ Add Member", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(members) { member ->
                    val summary = summaryMap[member.id]
                    val isExpanded = member.id == expandedMemberId
                    val color = MemberColors[member.colorIndex % MemberColors.size]

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedMemberId = if (isExpanded) null else member.id
                            }
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(color))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = member.name, style = MaterialTheme.typography.titleLarge)
                                    Text(
                                        text = if (member.isActive) "Active Member" else "Inactive",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))

                                val net = summary?.netBalance ?: 0L
                                val netStr = String.format("%.2f", abs(net / 100.0))
                                Text(
                                    text = if (net >= 0) "+$currencySymbol$netStr" else "-$currencySymbol$netStr",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (net >= 0) HerbGreen else ChiliCoral
                                )
                            }

                            // Personal Statement Breakdown
                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 16.dp)) {
                                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(text = "Personal Statement", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    StatementRow("Total Meals", "${summary?.totalMeals ?: 0}")
                                    StatementRow("Meal Cost", "$currencySymbol${String.format("%.2f", (summary?.mealCost ?: 0L) / 100.0)}")
                                    StatementRow("Fixed Bill Shares", "$currencySymbol${String.format("%.2f", (summary?.billShares ?: 0L) / 100.0)}")
                                    StatementRow("Paid Out", "$currencySymbol${String.format("%.2f", (summary?.paidOut ?: 0L) / 100.0)}")
                                    StatementRow("Payments Made", "$currencySymbol${String.format("%.2f", (summary?.paymentsMade ?: 0L) / 100.0)}")
                                    StatementRow("Payments Received", "$currencySymbol${String.format("%.2f", (summary?.paymentsReceived ?: 0L) / 100.0)}")

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                        TextButton(
                                            onClick = {
                                                onUpdateMember(member.copy(isActive = !member.isActive))
                                            }
                                        ) {
                                            Text(
                                                text = if (member.isActive) "Mark Inactive" else "Mark Active",
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var newName by remember { mutableStateOf("") }
        var selectedColorIdx by remember { mutableStateOf(members.size % 8) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Member") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Member Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Select Avatar Color", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MemberColors.take(6).forEachIndexed { idx, c ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .clickable { selectedColorIdx = idx }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onAddMember(newName.trim(), selectedColorIdx)
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatementRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
