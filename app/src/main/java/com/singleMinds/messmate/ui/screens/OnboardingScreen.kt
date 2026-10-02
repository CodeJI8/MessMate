package com.singleminds.messmate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.singleminds.messmate.ui.components.MessMateBackground
import com.singleminds.messmate.ui.theme.MemberColors
import com.singleminds.messmate.ui.theme.Saffron

@Composable
fun OnboardingScreen(
    onStartFlat: (name: String, currencySymbol: String, members: List<String>) -> Unit,
    onLoadSample: () -> Unit,
    modifier: Modifier = Modifier
) {
    var flatName by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("$") }
    var memberInput by remember { mutableStateOf("") }
    val members = remember { mutableStateListOf<String>() }

    val currencies = listOf("$", "৳", "€", "₹", "£")

    MessMateBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Welcome to Messmate",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "The Warm Kitchen Ledger for shared meals and bills.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Flat Details", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = flatName,
                            onValueChange = { flatName = it },
                            label = { Text("Flat or Mess Name") },
                            placeholder = { Text("e.g. Baker Street Kitchen") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Currency Symbol", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            currencies.forEach { sym ->
                                val isSelected = sym == selectedCurrency
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Saffron else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                        .clickable { selectedCurrency = sym },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sym,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Add Flatmates", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = memberInput,
                                onValueChange = { memberInput = it },
                                label = { Text("Member Name") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (memberInput.isNotBlank()) {
                                        members.add(memberInput.trim())
                                        memberInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(56.dp)
                            ) {
                                Text("Add")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            itemsIndexed(members) { idx, name ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MemberColors[idx % MemberColors.size])
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = name, style = MaterialTheme.typography.titleSmall)
                    }
                    Text(
                        text = "Remove",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.clickable { members.removeAt(idx) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        if (flatName.isNotBlank() && members.isNotEmpty()) {
                            onStartFlat(flatName, selectedCurrency, members)
                        }
                    },
                    enabled = flatName.isNotBlank() && members.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                ) {
                    Text(text = "Start The Table", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onLoadSample,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(text = "Load Sample Flat", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
