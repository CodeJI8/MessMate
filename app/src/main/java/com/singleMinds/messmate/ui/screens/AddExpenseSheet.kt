package com.singleminds.messmate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import com.singleminds.messmate.data.local.entity.ExpenseType
import com.singleminds.messmate.data.local.entity.Member
import com.singleminds.messmate.data.local.entity.SplitRule
import com.singleminds.messmate.ui.theme.MemberColors
import com.singleminds.messmate.ui.theme.Saffron
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseSheet(
    members: List<Member>,
    currencySymbol: String,
    onSaveExpense: (
        title: String,
        amountMinor: Long,
        payerId: String,
        type: ExpenseType,
        category: String,
        splitRule: SplitRule,
        note: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var titleText by remember { mutableStateOf("") }
    var selectedPayerId by remember { mutableStateOf(members.firstOrNull()?.id ?: "") }
    var selectedType by remember { mutableStateOf(ExpenseType.MEAL_POOL) }
    var selectedCategory by remember { mutableStateOf("Grocery") }
    var selectedSplitRule by remember { mutableStateOf(SplitRule.EQUAL) }
    var noteText by remember { mutableStateOf("") }

    val categories = listOf("Grocery", "Utilities", "Gas", "Rent", "Snacks", "Other")

    val amountMajor = amountText.toDoubleOrNull() ?: 0.0
    val amountMinor = (amountMajor * 100).toLong()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Add Expense",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Large Display Amount view
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                val formattedAmount = String.format(Locale.getDefault(), "%.2f", amountMajor)
                Text(
                    text = "$currencySymbol$formattedAmount",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount ($currencySymbol)") },
                placeholder = { Text("e.g. 1200") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = titleText,
                onValueChange = { titleText = it },
                label = { Text("Title") },
                placeholder = { Text("e.g. Weekly Grocery or Electricity Bill") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Paid By", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(members) { member ->
                    val isSelected = member.id == selectedPayerId
                    val color = MemberColors[member.colorIndex % MemberColors.size]
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPayerId = member.id },
                        label = { Text(member.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Expense Type", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedType == ExpenseType.MEAL_POOL) Saffron else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        .clickable { selectedType = ExpenseType.MEAL_POOL },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Meal Pool",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selectedType == ExpenseType.MEAL_POOL) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedType == ExpenseType.FIXED_BILL) Saffron else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        .clickable { selectedType = ExpenseType.FIXED_BILL },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Fixed Bill",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selectedType == ExpenseType.FIXED_BILL) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Category", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (amountMinor > 0L && titleText.isNotBlank() && selectedPayerId.isNotBlank()) {
                        onSaveExpense(
                            titleText,
                            amountMinor,
                            selectedPayerId,
                            selectedType,
                            selectedCategory,
                            selectedSplitRule,
                            noteText
                        )
                        onDismiss()
                    }
                },
                enabled = amountMinor > 0L && titleText.isNotBlank() && selectedPayerId.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Saffron)
            ) {
                Text(text = "Save Expense", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
