package com.pheeeew.feature.map.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pheeeew.feature.map.map.MapScreenPoint
import kotlinx.coroutines.delay

@Composable
fun LandingHighlightOverlay(
    featureIds: List<String>,
    projectedPoints: Map<String, MapScreenPoint>,
    onExpired: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    featureIds.forEach { id ->
        key(id) {
            projectedPoints[id]?.let { point ->
                var alpha by remember { mutableStateOf(1f) }
                LaunchedEffect(Unit) {
                    delay(10_000)
                    Animatable(1f).animateTo(0f, tween(260)) { alpha = value }
                    onExpired(id)
                }
                Canvas(modifier) {
                    val center = Offset(point.xPx, point.yPx)
                    drawCircle(
                        brush =
                            Brush.radialGradient(
                                listOf(Color.White.copy(alpha = alpha * 0.7f), Color.Transparent),
                                radius = 24.dp.toPx(),
                            ),
                        radius = 24.dp.toPx(),
                        center = center,
                    )
                    drawCircle(Color(0xFFFFE4A8).copy(alpha = alpha), radius = 5.5.dp.toPx(), center = center)
                }
            }
        }
    }
}
