package com.pheeeew.feature.map

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pheeeew.core.designsystem.theme.AppColors
import com.pheeeew.core.designsystem.theme.AppTheme
import kotlinx.coroutines.delay

private const val LONG_PRESS_THRESHOLD_MILLIS = 500L

@Composable
fun SighButton(
    onLongPressRelease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val minSize = 88.dp
    val maxSize = 140.dp
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val size by animateDpAsState(
        targetValue = if (isPressed) maxSize else minSize,
        animationSpec = tween(durationMillis = if (isPressed) 3000 else 300),
        label = "sighButtonSize",
    )

    // 누르는 동안 커지다가, 0.5초 이상 누른 뒤 손을 뗀 시점에만 콜백을 호출한다.
    var heldLongEnough by remember { mutableStateOf(false) }
    LaunchedEffect(isPressed) {
        if (isPressed) {
            heldLongEnough = false
            delay(LONG_PRESS_THRESHOLD_MILLIS)
            heldLongEnough = true
        } else if (heldLongEnough) {
            heldLongEnough = false
            onLongPressRelease()
        }
    }

    Box(
        modifier = modifier.size(maxSize),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(
                        brush =
                            Brush.radialGradient(
                                0.0f to AppColors.Tan200,
                                0.35f to AppColors.Cream100,
                                0.7f to AppColors.Blue100,
                                1.0f to AppColors.Blue200.copy(alpha = 0f),
                            ),
                    ).clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {},
                    ),
        )
    }
}

@Preview
@Composable
private fun SighButtonPreview() {
    AppTheme {
        Box(modifier = Modifier.background(AppColors.Navy900).padding(24.dp)) {
            SighButton(onLongPressRelease = {})
        }
    }
}
