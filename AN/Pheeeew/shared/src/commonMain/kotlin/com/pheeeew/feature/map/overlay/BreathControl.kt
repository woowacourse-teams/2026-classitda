package com.pheeeew.feature.map.overlay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
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
import kotlin.math.roundToInt
import kotlin.math.sin

private const val EFFECTIVE_STRENGTH_THRESHOLD = 0.22f
private const val GROWTH_DURATION_MILLIS = 2_200L
private const val BURST_DURATION_MILLIS = 720L
private const val IDLE_SCALE = 2f / 3f
private const val SCALE_TWEEN_MILLIS = 220
private const val QUIET_DELAY_MILLIS = 500L
private const val MIN_RELEASE_GROWTH = 0.3f
private const val NEEDS_MORE_HINT_MILLIS = 1_400L
private const val RELEASE_DRAG_MAX_DP = 1200f
private const val FLING_VELOCITY_THRESHOLD_DP = 400f
private const val SHAKE_AMPLITUDE_DP = 3f
private const val FLY_AWAY_DISTANCE_DP = 1600f
private const val IDLE_TEXT_GAP_BOX_HEIGHT_DP = 100f
private const val BUTTON_BOTTOM_MARGIN_DP = 32f

enum class SighPhase { Idle, Listening, Quiet, NeedsMore, Bursting }

@Composable
fun BreathControl(
    enabled: Boolean,
    onExplosionFinished: (originInRoot: Offset) -> Unit,
    onMicrophoneError: (BreathInputError) -> Unit,
    ensureLocationPermission: suspend () -> Boolean,
    onPhaseChanged: (SighPhase) -> Unit,
    cancelSignal: Int,
    modifier: Modifier = Modifier,
    requestPermissionOnLaunch: Boolean = true,
) {
    var listening by remember { mutableStateOf(false) }
    var strength by remember { mutableStateOf(0f) }
    var growth by remember { mutableStateOf(0f) }
    var activeElapsedMillis by remember { mutableStateOf(0L) }
    var lastActiveElapsedMillis by remember { mutableStateOf(0L) }
    var burst by remember { mutableStateOf(false) }
    var burstSequence by remember { mutableStateOf(0) }
    var burstProgress by remember { mutableStateOf(0f) }
    var needsMoreActive by remember { mutableStateOf(false) }
    var needsMoreSequence by remember { mutableStateOf(0) }
    var origin by remember { mutableStateOf(Offset.Zero) }
    var burstOrigin by remember { mutableStateOf(Offset.Zero) }
    val dragOffsetY = remember { Animatable(0f) }
    val breathInput = rememberBreathInput()
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

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
                    activeElapsedMillis = 0L
                    lastActiveElapsedMillis = 0L
                    coroutineScope.launch { dragOffsetY.snapTo(0f) }
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
                activeElapsedMillis = (activeElapsedMillis + 16L).coerceAtMost(GROWTH_DURATION_MILLIS)
                val progress = activeElapsedMillis / GROWTH_DURATION_MILLIS.toFloat()
                val remaining = 1f - progress
                growth = 1f - remaining * remaining * remaining
                lastActiveElapsedMillis = 0L
            } else {
                lastActiveElapsedMillis += 16L
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
        onExplosionFinished(burstOrigin)
        burst = false
        burstProgress = 0f
        growth = 0f
        strength = 0f
        activeElapsedMillis = 0L
        lastActiveElapsedMillis = 0L
        dragOffsetY.snapTo(0f)
    }

    LaunchedEffect(needsMoreSequence) {
        if (needsMoreSequence == 0) return@LaunchedEffect
        needsMoreActive = true
        delay(NEEDS_MORE_HINT_MILLIS)
        needsMoreActive = false
    }

    LaunchedEffect(cancelSignal) {
        if (cancelSignal > 0 && listening) {
            breathInput.stop()
            listening = false
            growth = 0f
            strength = 0f
            activeElapsedMillis = 0L
            lastActiveElapsedMillis = 0L
            dragOffsetY.snapTo(0f)
        }
    }

    val phase =
        when {
            burst -> SighPhase.Bursting
            !listening -> SighPhase.Idle
            needsMoreActive -> SighPhase.NeedsMore
            growth >= 1f -> SighPhase.Quiet
            growth > 0f && lastActiveElapsedMillis >= QUIET_DELAY_MILLIS -> SighPhase.Quiet
            else -> SighPhase.Listening
        }
    LaunchedEffect(phase) { onPhaseChanged(phase) }

    // TODO
    val controlDescription =
        when {
            burst -> "한숨을 별로 만드는 중"
            listening -> "한숨 감지 중. 위로 슬라이드하면 별을 날려보냅니다"
            else -> "한숨 감지 시작"
        }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (!listening && !burst) {
            Text(
                text = "한숨 내쉬기",
                style = AppTheme.typography.menuItem,
                color = AppColors.Cream100,
            )
        }
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = BUTTON_BOTTOM_MARGIN_DP.dp)
                    .height(IDLE_TEXT_GAP_BOX_HEIGHT_DP.dp),
            contentAlignment = Alignment.Center,
        ) {
            val maxDiameter = (maxWidth.value * 0.88f).coerceAtMost(360f)
            val maxScale = (maxDiameter / 124f).coerceAtLeast(1f)
            val targetScale =
                when {
                    burst -> 1f
                    listening -> 1f + growth * (maxScale - 1f)
                    else -> IDLE_SCALE
                }
            val scale by animateFloatAsState(
                targetValue = targetScale,
                animationSpec =
                    if (burst) {
                        // Match the fly-away's duration and easing exactly so the shrink and the upward
                        // launch complete in lockstep instead of the shrink finishing early and leaving
                        // the button sitting there, barely moved, until the (slower-starting) flight catches up.
                        tween(durationMillis = BURST_DURATION_MILLIS.toInt(), easing = FastOutLinearInEasing)
                    } else {
                        tween(durationMillis = SCALE_TWEEN_MILLIS, easing = FastOutSlowInEasing)
                    },
                label = "breathScale",
            )
            val particleDiameter = (maxWidth.value * 1.6f).coerceAtMost(680f)

            val isMaxedAndBlowing = listening && growth >= 1f && strength >= EFFECTIVE_STRENGTH_THRESHOLD
            val shakePhase by rememberInfiniteTransition(label = "maxShake").animateFloat(
                initialValue = -1f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(90, easing = LinearEasing), RepeatMode.Reverse),
                label = "maxShakeValue",
            )
            val shakeOffsetPx =
                if (isMaxedAndBlowing) {
                    shakePhase *
                        with(
                            density,
                        ) { SHAKE_AMPLITUDE_DP.dp.toPx() }
                } else {
                    0f
                }
            // Center alignment keeps the box's center point fixed as it resizes; shifting it up by
            // half the size delta from the 124dp reference makes the bottom edge stay put instead,
            // so the button visually grows upward from a fixed base rather than from its center.
            val bottomAnchorShiftPx = with(density) { (62f * (1f - scale)).dp.toPx() }

            Box(
                modifier =
                    Modifier
                        .offset {
                            IntOffset(
                                shakeOffsetPx.roundToInt(),
                                (bottomAnchorShiftPx + dragOffsetY.value).roundToInt(),
                            )
                        }.requiredSize(124.dp * scale)
                        .onGloballyPositioned { coordinates ->
                            val point = coordinates.positionInRoot()
                            origin =
                                Offset(point.x + coordinates.size.width / 2f, point.y + coordinates.size.height / 2f)
                        }.pointerInput(enabled, listening, burst) {
                            if (!listening) {
                                detectTapGestures(onTap = {
                                    if (!enabled || burst) return@detectTapGestures
                                    coroutineScope.launch {
                                        if (!breathInput.requestPermission()) {
                                            onMicrophoneError(BreathInputError.PermissionDenied)
                                            return@launch
                                        }
                                        if (!ensureLocationPermission()) return@launch
                                        growth = 0f
                                        strength = 0f
                                        activeElapsedMillis = 0L
                                        lastActiveElapsedMillis = 0L
                                        listening = true
                                        breathInput.start(
                                            onStrengthChanged = { strength = it },
                                            onError = { error ->
                                                onMicrophoneError(error)
                                                breathInput.stop()
                                                listening = false
                                                growth = 0f
                                                strength = 0f
                                                activeElapsedMillis = 0L
                                            },
                                        )
                                    }
                                })
                            } else {
                                val maxDragPx = with(density) { RELEASE_DRAG_MAX_DP.dp.toPx() }
                                val flingThresholdPx = with(density) { FLING_VELOCITY_THRESHOLD_DP.dp.toPx() }
                                val velocityTracker = VelocityTracker()
                                detectDragGestures(
                                    onDragStart = { velocityTracker.resetTracking() },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        velocityTracker.addPointerInputChange(change)
                                        coroutineScope.launch {
                                            dragOffsetY.snapTo(
                                                (dragOffsetY.value + dragAmount.y).coerceIn(-maxDragPx, 0f),
                                            )
                                        }
                                    },
                                    onDragEnd = {
                                        // A slow, deliberate raise must NOT register — only a fast upward
                                        // flick (fling) does, regardless of how far the drag itself traveled.
                                        val flingVelocityY = velocityTracker.calculateVelocity().y
                                        when {
                                            flingVelocityY > -flingThresholdPx -> {
                                                coroutineScope.launch {
                                                    dragOffsetY.animateTo(
                                                        0f,
                                                        spring(dampingRatio = 0.7f, stiffness = 300f),
                                                    )
                                                }
                                            }

                                            growth < MIN_RELEASE_GROWTH -> {
                                                needsMoreSequence += 1
                                                coroutineScope.launch {
                                                    dragOffsetY.animateTo(
                                                        0f,
                                                        spring(dampingRatio = 0.7f, stiffness = 300f),
                                                    )
                                                }
                                            }

                                            else -> {
                                                breathInput.stop()
                                                listening = false
                                                burstOrigin = origin
                                                burst = true
                                                burstSequence += 1
                                                coroutineScope.launch {
                                                    dragOffsetY.animateTo(
                                                        targetValue = -with(density) { FLY_AWAY_DISTANCE_DP.dp.toPx() },
                                                        animationSpec =
                                                            tween(
                                                                durationMillis = BURST_DURATION_MILLIS.toInt(),
                                                                easing = FastOutLinearInEasing,
                                                            ),
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onDragCancel = {
                                        coroutineScope.launch {
                                            dragOffsetY.animateTo(0f, spring(dampingRatio = 0.7f, stiffness = 300f))
                                        }
                                    },
                                )
                            }
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
    }
}

@Preview(showBackground = true)
@Composable
private fun BreathControlPreview() {
    BreathControl(
        enabled = true,
        onExplosionFinished = {},
        onMicrophoneError = {},
        ensureLocationPermission = { true },
        onPhaseChanged = {},
        cancelSignal = 0,
    )
}
