package com.singleminds.messmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.singleminds.messmate.ui.theme.Saffron

enum class NavDestination { Home, Meals, Ledger, Members, Settle, MonthClose, Settings }

@Composable
fun FloatingPillDock(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp), contentAlignment = Alignment.BottomCenter) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(32.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                DockItem("Home", currentDestination == NavDestination.Home) { onNavigate(NavDestination.Home) }
                DockItem("Meals", currentDestination == NavDestination.Meals) { onNavigate(NavDestination.Meals) }
            }
            Spacer(modifier = Modifier.width(64.dp))
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                DockItem("Ledger", currentDestination == NavDestination.Ledger) { onNavigate(NavDestination.Ledger) }
                DockItem("Members", currentDestination == NavDestination.Members) { onNavigate(NavDestination.Members) }
            }
        }
        Box(
            modifier = Modifier
                .offset(y = (-16).dp)
                .size(64.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(Saffron)
                .clickable { onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "+", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun DockItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.padding(6.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = color)
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(color))
        }
    }
}
