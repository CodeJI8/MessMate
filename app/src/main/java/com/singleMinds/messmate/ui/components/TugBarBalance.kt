package com.singleminds.messmate.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.singleminds.messmate.ui.theme.ChiliCoral
import com.singleminds.messmate.ui.theme.HerbGreen
import java.util.Locale
import kotlin.math.abs

@Composable
fun TugBarBalance(
    memberName: String,
    memberColor: Color,
    balanceMinor: Long,
    maxAbsBalanceMinor: Long,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val maxBound = maxOf(maxAbsBalanceMinor, 1L).toFloat()
    // Coerce normalized balance slightly inside (-1f, 1f) so weights are never zero
    val normalizedBalance = (balanceMinor.toFloat() / maxBound).coerceIn(-0.999f, 0.999f)
    
    val animatedBalance by animateFloatAsState(
        targetValue = normalizedBalance,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "TugBarAnim"
    )

    Row(
        modifier = modifier.fillMaxWidth().height(48.dp).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = memberName, style = MaterialTheme.typography.titleMedium, color = memberColor, modifier = Modifier.width(80.dp), maxLines = 1)
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)))
            
            val absBal = abs(animatedBalance).coerceIn(0.001f, 0.999f)
            
            if (animatedBalance < 0f) {
                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.weight((1f - absBal).coerceAtLeast(0.001f)))
                    Box(modifier = Modifier.weight(absBal).height(12.dp).clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)).background(ChiliCoral))
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            if (animatedBalance > 0f) {
                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.weight(absBal).height(12.dp).clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp)).background(HerbGreen))
                    Spacer(modifier = Modifier.weight((1f - absBal).coerceAtLeast(0.001f)))
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        val balanceStr = String.format(Locale.getDefault(), "%.2f", abs(balanceMinor / 100.0))
        Text(text = "$currencySymbol$balanceStr", style = MaterialTheme.typography.titleMedium, color = if (balanceMinor >= 0) HerbGreen else ChiliCoral, modifier = Modifier.width(72.dp), textAlign = TextAlign.End, maxLines = 1)
    }
}
