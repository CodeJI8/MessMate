package com.singleminds.messmate.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.atan2

data class PlateSegment(
    val memberId: Long,
    val amount: Long,
    val color: Color
)

@Composable
fun PlateVisual(
    mealRate: Double,
    segments: List<PlateSegment>,
    currencySymbol: String,
    modifier: Modifier = Modifier,
    onSegmentTapped: ((Long) -> Unit)? = null
) {
    val totalAmount = segments.sumOf { it.amount }
    
    val transition = updateTransition(targetState = true, label = "PlateAnimation")
    val sweepProgress by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow) },
        label = "SweepProgress"
    ) { state -> if (state) 1f else 0f }
    
    var currentStartAngle = -90f
    val segmentAngles = segments.map { segment ->
        val sweep = if (totalAmount > 0) (segment.amount.toFloat() / totalAmount.toFloat()) * 360f else 0f
        val angleData = Triple(segment.memberId, currentStartAngle, sweep)
        currentStartAngle += sweep
        angleData
    }

    Box(
        modifier = modifier.aspectRatio(1f).padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val backgroundColor = MaterialTheme.colorScheme.surface
        val onBackgroundColor = MaterialTheme.colorScheme.onSurface
        
        Canvas(modifier = Modifier.fillMaxSize().pointerInput(segmentAngles) {
            detectTapGestures { offset ->
                val center = Offset(size.width / 2f, size.height / 2f)
                val dx = offset.x - center.x
                val dy = offset.y - center.y
                var angle = (atan2(dy.toDouble(), dx.toDouble()) * (180 / Math.PI)).toFloat()
                if (angle < 0) angle += 360f
                var shiftedAngle = angle + 90f
                if (shiftedAngle >= 360f) shiftedAngle -= 360f
                var currentCheckAngle = 0f
                for ((memberId, _, sweep) in segmentAngles) {
                    if (shiftedAngle >= currentCheckAngle && shiftedAngle < currentCheckAngle + sweep) {
                        onSegmentTapped?.invoke(memberId)
                        break
                    }
                    currentCheckAngle += sweep
                }
            }
        }) {
            val strokeWidth = size.width * 0.12f
            val plateRadius = (size.minDimension - strokeWidth) / 2f
            
            drawCircle(color = onBackgroundColor.copy(alpha = 0.05f), radius = plateRadius, style = Stroke(width = strokeWidth))
            drawCircle(color = onBackgroundColor.copy(alpha = 0.02f), radius = plateRadius - (strokeWidth / 2f))
            
            for (i in segments.indices) {
                val (_, startAngle, sweep) = segmentAngles[i]
                val segment = segments[i]
                val gap = if (segments.size > 1 && sweep > 2f) 2f else 0f
                val actualSweep = maxOf(0f, sweep - gap)
                val animatedSweep = actualSweep * sweepProgress
                
                if (animatedSweep > 0) {
                    drawArc(
                        color = segment.color,
                        startAngle = startAngle + (gap/2f),
                        sweepAngle = animatedSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = Size(plateRadius * 2, plateRadius * 2),
                        topLeft = Offset(center.x - plateRadius, center.y - plateRadius)
                    )
                }
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$currencySymbol${String.format("%.2f", mealRate)}", style = MaterialTheme.typography.displayMedium)
            Text(text = "per meal", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}
