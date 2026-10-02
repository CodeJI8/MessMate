package com.singleminds.messmate.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.singleminds.messmate.data.local.entity.MealEntry
import com.singleminds.messmate.data.local.entity.Member
import com.singleminds.messmate.ui.components.MessMateBackground
import com.singleminds.messmate.ui.theme.HerbGreen
import com.singleminds.messmate.ui.theme.MemberColors
import com.singleminds.messmate.ui.theme.Saffron
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MealsScreen(
    members: List<Member>,
    mealEntries: List<MealEntry>,
    onSaveMeal: (memberId: String, date: Long, breakfast: Int, lunch: Int, dinner: Int) -> Unit,
    onEveryoneAte: (date: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val zone = ZoneId.systemDefault()
    val selectedDateMillis = selectedDate.atStartOfDay(zone).toInstant().toEpochMilli()

    MessMateBackground(modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize().padding(top = 48.dp)) {
            // Screen Title
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Meals Log", style = MaterialTheme.typography.displaySmall)
                Button(
                    onClick = { onEveryoneAte(selectedDateMillis) },
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(text = "Everyone Ate", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Horizontal Day Strip Calendar
            val daysInMonth = (1..selectedDate.lengthOfMonth()).map { day ->
                selectedDate.withDayOfMonth(day)
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(daysInMonth) { date ->
                    val isSelected = date == selectedDate
                    val dayNum = date.dayOfMonth.toString()
                    val dayName = date.format(DateTimeFormatter.ofPattern("EEE"))

                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Saffron else MaterialTheme.colorScheme.surface)
                            .clickable { selectedDate = date },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dayNum,
                                style = MaterialTheme.typography.titleLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Member B/L/D Cards
            LazyColumn(
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(members.filter { it.isActive }) { member ->
                    val entry = mealEntries.find { it.memberId == member.id && isSameDay(it.date, selectedDateMillis) }
                    val bCount = entry?.breakfast ?: 0
                    val lCount = entry?.lunch ?: 0
                    val dCount = entry?.dinner ?: 0

                    val memberColor = MemberColors[member.colorIndex % MemberColors.size]

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(memberColor))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = member.name, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.weight(1f))
                                val total = bCount + lCount + dCount
                                Text(
                                    text = "$total meal${if (total != 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MealPillChip(
                                    label = "Breakfast",
                                    count = bCount,
                                    onToggle = { newCount -> onSaveMeal(member.id, selectedDateMillis, newCount, lCount, dCount) },
                                    modifier = Modifier.weight(1f)
                                )
                                MealPillChip(
                                    label = "Lunch",
                                    count = lCount,
                                    onToggle = { newCount -> onSaveMeal(member.id, selectedDateMillis, bCount, newCount, dCount) },
                                    modifier = Modifier.weight(1f)
                                )
                                MealPillChip(
                                    label = "Dinner",
                                    count = dCount,
                                    onToggle = { newCount -> onSaveMeal(member.id, selectedDateMillis, bCount, lCount, newCount) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MealPillChip(
    label: String,
    count: Int,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isEaten = count > 0
    val scale by animateFloatAsState(
        targetValue = if (isEaten) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "PillScale"
    )

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isEaten) HerbGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            .pointerInput(count) {
                detectTapGestures(
                    onTap = {
                        onToggle(if (count == 0) 1 else 0)
                    },
                    onLongPress = {
                        onToggle((count + 1) % 5) // cycle guest meals 0..4
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isEaten) Color.Black else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = if (count > 1) "$count meals" else if (isEaten) "Eaten" else "Skip",
                style = MaterialTheme.typography.labelMedium,
                color = if (isEaten) Color.Black else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun isSameDay(t1: Long, t2: Long): Boolean {
    val zone = ZoneId.systemDefault()
    val d1 = Instant.ofEpochMilli(t1).atZone(zone).toLocalDate()
    val d2 = Instant.ofEpochMilli(t2).atZone(zone).toLocalDate()
    return d1 == d2
}
