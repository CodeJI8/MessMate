package com.singleminds.messmate.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.singleminds.messmate.ui.theme.Saffron

@Composable
fun EmptyStateIllustration(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val primaryColor = Saffron
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface

        Canvas(modifier = Modifier.size(120.dp)) {
            val centerOffset = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2.5f

            // Outer subtle plate
            drawCircle(
                color = onSurfaceColor.copy(alpha = 0.08f),
                radius = radius,
                style = Stroke(width = 4.dp.toPx())
            )

            // Inner fork & spoon outline
            val handleLength = radius * 0.8f
            // Fork tines
            drawLine(
                color = primaryColor.copy(alpha = 0.6f),
                start = Offset(centerOffset.x - 12.dp.toPx(), centerOffset.y - 16.dp.toPx()),
                end = Offset(centerOffset.x - 12.dp.toPx(), centerOffset.y + handleLength),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Spoon head
            drawArc(
                color = primaryColor.copy(alpha = 0.6f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(centerOffset.x + 4.dp.toPx(), centerOffset.y - 20.dp.toPx()),
                size = Size(16.dp.toPx(), 24.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )
            drawLine(
                color = primaryColor.copy(alpha = 0.6f),
                start = Offset(centerOffset.x + 12.dp.toPx(), centerOffset.y + 4.dp.toPx()),
                end = Offset(centerOffset.x + 12.dp.toPx(), centerOffset.y + handleLength),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
