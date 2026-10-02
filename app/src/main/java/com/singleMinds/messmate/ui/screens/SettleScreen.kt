package com.singleminds.messmate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.singleminds.messmate.data.local.entity.Member
import com.singleminds.messmate.engine.SettlementTransfer
import com.singleminds.messmate.ui.components.EmptyStateIllustration
import com.singleminds.messmate.ui.components.MessMateBackground
import com.singleminds.messmate.ui.theme.HerbGreen
import com.singleminds.messmate.ui.theme.MemberColors
import com.singleminds.messmate.ui.theme.Saffron

@Composable
fun SettleScreen(
    settlements: List<SettlementTransfer>,
    members: List<Member>,
    currencySymbol: String,
    onRecordPayment: (fromMemberId: String, toMemberId: String, amountMinor: Long, note: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSettlement by remember { mutableStateOf<SettlementTransfer?>(null) }

    MessMateBackground(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize().padding(top = 48.dp)) {
            Text(
                text = "Settle Up",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Minimum transfers to clear all flat balances.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (settlements.isEmpty()) {
                EmptyStateIllustration(
                    title = "All Settled Up!",
                    subtitle = "Everyone is square. No pending transfers needed."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(settlements) { transfer ->
                        val debtor = members.find { it.id == transfer.fromMemberId }
                        val creditor = members.find { it.id == transfer.toMemberId }

                        val debtorColor = if (debtor != null) MemberColors[debtor.colorIndex % MemberColors.size] else Saffron
                        val creditorColor = if (creditor != null) MemberColors[creditor.colorIndex % MemberColors.size] else HerbGreen

                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Debtor Avatar
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier.size(48.dp).clip(CircleShape).background(debtorColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = debtor?.name?.take(1) ?: "D",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = debtor?.name ?: "Debtor", style = MaterialTheme.typography.titleSmall)
                                    }

                                    // Amount Arrow
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        val amountStr = String.format("%.2f", transfer.amountMinor / 100.0)
                                        Text(
                                            text = "$currencySymbol$amountStr",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(text = "──────>", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }

                                    // Creditor Avatar
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier.size(48.dp).clip(CircleShape).background(creditorColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = creditor?.name?.take(1) ?: "C",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = creditor?.name ?: "Creditor", style = MaterialTheme.typography.titleSmall)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { activeSettlement = transfer },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = HerbGreen)
                                ) {
                                    Text(text = "Mark as Paid", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (activeSettlement != null) {
        val transfer = activeSettlement!!
        val debtor = members.find { it.id == transfer.fromMemberId }
        val creditor = members.find { it.id == transfer.toMemberId }

        var paymentAmountText by remember { mutableStateOf(transfer.amountMinor.toString()) }
        var noteText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { activeSettlement = null },
            title = { Text("Record Payment") },
            text = {
                Column {
                    Text(
                        text = "Record payment from ${debtor?.name} to ${creditor?.name}.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = paymentAmountText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) paymentAmountText = it },
                        label = { Text("Amount (in cents e.g. 500 = $5.00)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Note (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = paymentAmountText.toLongOrNull() ?: 0L
                        if (amount > 0L) {
                            onRecordPayment(transfer.fromMemberId, transfer.toMemberId, amount, noteText)
                            activeSettlement = null
                        }
                    }
                ) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeSettlement = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
