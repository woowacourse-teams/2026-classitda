package com.pheeeew.feature.map.overlay

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pheeeew.core.designsystem.theme.AppColors
import com.pheeeew.core.designsystem.theme.AppTheme

private const val BOB_DURATION_MILLIS = 650
private const val BOB_DISTANCE_DP = 10f

@Composable
fun SwipeUpHint(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "swipeUpHint")
    val bobDp by transition.animateFloat(
        initialValue = 0f,
        targetValue = -BOB_DISTANCE_DP,
        animationSpec =
            infiniteRepeatable(
                tween(BOB_DURATION_MILLIS, easing = FastOutSlowInEasing),
                RepeatMode.Reverse,
            ),
        label = "swipeUpHintBob",
    )
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                tween(BOB_DURATION_MILLIS, easing = FastOutSlowInEasing),
                RepeatMode.Reverse,
            ),
        label = "swipeUpHintAlpha",
    )
    Canvas(
        modifier =
            modifier
                .size(28.dp)
                .offset(y = bobDp.dp)
                .alpha(alpha),
    ) {
        val strokeWidth = 3.dp.toPx()
        val path =
            Path().apply {
                moveTo(size.width * 0.12f, size.height * 0.72f)
                lineTo(size.width / 2f, size.height * 0.18f)
                lineTo(size.width * 0.88f, size.height * 0.72f)
            }
        drawPath(
            path,
            color = AppColors.Cream100,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

@Preview
@Composable
private fun SwipeUpHintPreview() {
    AppTheme {
        Box(modifier = Modifier.background(AppTheme.colors.background).padding(24.dp)) {
            SwipeUpHint()
        }
    }
}
