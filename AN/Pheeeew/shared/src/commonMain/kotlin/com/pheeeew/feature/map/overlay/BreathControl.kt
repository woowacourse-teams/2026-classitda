package com.pheeeew.feature.map.overlay

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pheeeew.core.audio.BreathInputError
import com.pheeeew.core.audio.rememberBreathInput
import com.pheeeew.core.designsystem.theme.AppColors
import com.pheeeew.core.designsystem.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val EFFECTIVE_STRENGTH_THRESHOLD = 0.22f
private const val GROWTH_DURATION_MILLIS = 567L
private const val BURST_DURATION_MILLIS = 720L

@Composable
fun BreathControl(
    enabled: Boolean,
    onExplosionFinished: (originInRoot: Offset) -> Unit,
    onMicrophoneError: (BreathInputError) -> Unit,
    ensureLocationPermission: suspend () -> Boolean,
    modifier: Modifier = Modifier,
    requestPermissionOnLaunch: Boolean = true,
) {
    var listening by remember { mutableStateOf(false) }
    var strength by remember { mutableStateOf(0f) }
    var growth by remember { mutableStateOf(0f) }
    var burst by remember { mutableStateOf(false) }
    var burstSequence by remember { mutableStateOf(0) }
    var burstProgress by remember { mutableStateOf(0f) }
    var origin by remember { mutableStateOf(Offset.Zero) }
    val breathInput = rememberBreathInput()
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(breathInput, requestPermissionOnLaunch) {
        if (requestPermissionOnLaunch) {
            breathInput.requestPermission()
        }
    }

    DisposableEffect(breathInput, lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) {
                    breathInput.stop()
                    listening = false
                    strength = 0f
                    growth = 0f
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            breathInput.stop()
        }
    }

    LaunchedEffect(listening) {
        while (listening) {
            delay(16)
            if (strength >= EFFECTIVE_STRENGTH_THRESHOLD) {
                growth = (growth + 16f / GROWTH_DURATION_MILLIS).coerceAtMost(1f)
            }
        }
    }

    LaunchedEffect(burstSequence) {
        if (burstSequence == 0) return@LaunchedEffect
        burstProgress = 0f
        val start =
            kotlin.time.TimeSource.Monotonic
                .markNow()
        while (burstProgress < 1f) {
            delay(16)
            burstProgress = (start.elapsedNow().inWholeMilliseconds.toFloat() / BURST_DURATION_MILLIS).coerceIn(0f, 1f)
        }
        onExplosionFinished(origin)
        burst = false
        burstProgress = 0f
        growth = 0f
        strength = 0f
    }

    val pulse by rememberInfiniteTransition(label = "breathPulse").animateFloat(
        initialValue = 0.96f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(tween(1_800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathPulseValue",
    )
    val controlDescription =
        when {
            burst -> "한숨을 별로 만드는 중"
            listening -> "한숨 감지 중. 다시 눌러 중지"
            else -> "한숨 감지 시작"
        }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().height(224.dp),
            contentAlignment = Alignment.Center,
        ) {
            val maxDiameter = (maxWidth.value * 0.88f).coerceAtMost(360f)
            val maxScale = (maxDiameter / 124f).coerceAtLeast(1f)
            val targetScale =
                when {
                    burst -> 1f
                    listening -> 1f + growth * (maxScale - 1f)
                    else -> pulse
                }
            val scale by animateFloatAsState(
                targetValue = targetScale,
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 180f),
                label = "breathScale",
            )
            val particleDiameter = (maxWidth.value * 1.6f).coerceAtMost(680f)
            Box(
                modifier =
                    Modifier
                        .requiredSize(124.dp * scale)
                        .onGloballyPositioned { coordinates ->
                            val point = coordinates.positionInRoot()
                            origin =
                                Offset(point.x + coordinates.size.width / 2f, point.y + coordinates.size.height / 2f)
                        }.pointerInput(enabled, listening, burst) {
                            detectTapGestures(onTap = {
                                if (!enabled || burst) return@detectTapGestures
                                if (!listening) {
                                    coroutineScope.launch {
                                        if (!breathInput.requestPermission()) {
                                            onMicrophoneError(BreathInputError.PermissionDenied)
                                            return@launch
                                        }
                                        if (!ensureLocationPermission()) return@launch
                                        growth = 0f
                                        strength = 0f
                                        listening = true
                                        breathInput.start(
                                            onStrengthChanged = { strength = it },
                                            onError = { error ->
                                                onMicrophoneError(error)
                                                breathInput.stop()
                                                listening = false
                                                growth = 0f
                                                strength = 0f
                                            },
                                        )
                                    }
                                } else {
                                    breathInput.stop()
                                    listening = false
                                    burst = true
                                    burstSequence += 1
                                }
                            })
                        }.semantics {
                            role = Role.Button
                            contentDescription = controlDescription
                        },
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.requiredSize(particleDiameter.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = 62.dp.toPx() * scale
                    val burstEase = 1f - (1f - burstProgress) * (1f - burstProgress) * (1f - burstProgress)
                    val highlightCenter =
                        Offset(
                            x = center.x - radius * 0.28f,
                            y = center.y - radius * 0.32f,
                        )
                    drawCircle(
                        brush =
                            Brush.radialGradient(
                                0.0f to Color.White.copy(alpha = 0.20f),
                                0.38f to AppColors.Cream100.copy(alpha = 0.18f),
                                0.63f to AppColors.Tan200.copy(alpha = 0.13f),
                                0.82f to AppColors.Blue100.copy(alpha = 0.10f),
                                1.0f to Color.Transparent,
                                center = center,
                                radius = radius * 1.72f,
                            ),
                        radius = radius * 1.72f,
                        center = center,
                    )
                    drawCircle(
                        brush =
                            Brush.radialGradient(
                                0.0f to AppColors.Cream100.copy(alpha = 0.16f),
                                0.52f to AppColors.Tan200.copy(alpha = 0.12f),
                                0.78f to AppColors.Blue100.copy(alpha = 0.08f),
                                1.0f to Color.Transparent,
                                center = Offset(center.x - radius * 0.10f, center.y - radius * 0.12f),
                                radius = radius * 1.38f,
                            ),
                        radius = radius * 1.38f,
                        center = center,
                    )
                    drawCircle(
                        brush =
                            Brush.radialGradient(
                                0.0f to Color.White.copy(alpha = 0.98f),
                                0.18f to AppColors.Cream100,
                                0.52f to AppColors.Tan200,
                                0.78f to AppColors.Blue100,
                                1.0f to AppColors.Blue200,
                                center = highlightCenter,
                                radius = radius * 1.28f,
                            ),
                        radius = radius,
                        center = center,
                    )
                    if (burstProgress > 0f) {
                        drawCircle(
                            Color.White.copy(alpha = (1f - burstProgress * 2.4f).coerceAtLeast(0f)),
                            radius * (0.4f + burstEase),
                            center,
                        )
                        repeat(18) { index ->
                            val angle = index * (2f * PI.toFloat() / 18f)
                            val distance = radius * (1.1f + burstEase * (2.0f + (index % 4) * 0.35f))
                            val point = Offset(center.x + cos(angle) * distance, center.y + sin(angle) * distance)
                            drawCircle(
                                Color.White.copy(alpha = (1f - burstProgress) * 0.85f),
                                radius =
                                    (
                                        1.5f +
                                            index % 3
                                    ).dp.toPx(),
                                center = point,
                            )
                        }
                    }
                }
            }
        }
        Text(
            text =
                when {
                    burst -> "한숨을 별로 빚고 있어요"
                    listening && growth >= 1f -> "한숨을 다 담았어요\n버튼을 눌러 별을 만들어보세요"
                    listening -> "후— 하고 불어보세요\n멈추려면 버튼을 눌러주세요"
                    else -> "버튼을 눌러 한숨 시작하기"
                },
            style = AppTheme.typography.caption,
            color = AppColors.Cream100,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BreathControlPreview() {
    BreathControl(
        enabled = true,
        onExplosionFinished = {},
        onMicrophoneError = {},
        ensureLocationPermission = { true },
    )
}
