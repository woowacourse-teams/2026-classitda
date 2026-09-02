package com.pheeeew.core.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object IosBreathBridge {
    private var startHandler: (() -> Unit)? = null
    private var stopHandler: (() -> Unit)? = null
    private var permissionHandler: (((Boolean) -> Unit) -> Unit)? = null
    private var strengthHandler: ((Float) -> Unit)? = null
    private var errorHandler: ((BreathInputError) -> Unit)? = null

    fun attach(
        onStart: () -> Unit,
        onStop: () -> Unit,
        onRequestPermission: (((Boolean) -> Unit) -> Unit),
    ) {
        startHandler = onStart
        stopHandler = onStop
        permissionHandler = onRequestPermission
    }

    internal fun start(
        onStrengthChanged: (Float) -> Unit,
        onError: (BreathInputError) -> Unit,
    ) {
        strengthHandler = onStrengthChanged
        errorHandler = onError
        startHandler?.invoke() ?: errorHandler?.invoke(BreathInputError.MicrophoneUnavailable)
    }

    internal suspend fun requestPermission(): Boolean =
        suspendCancellableCoroutine { continuation ->
            permissionHandler?.invoke { granted ->
                if (continuation.isActive) continuation.resume(granted)
            } ?: continuation.resume(false)
        }

    internal fun stop() {
        stopHandler?.invoke()
        strengthHandler?.invoke(0f)
        strengthHandler = null
        errorHandler =
            null
    }

    fun updateStrength(value: Double) {
        strengthHandler?.invoke(value.toFloat().coerceIn(0f, 1f))
    }

    fun updateError(errorName: String) {
        errorHandler?.invoke(
            BreathInputError.entries.firstOrNull { it.name == errorName } ?: BreathInputError.StartFailed,
        )
    }
}

@Composable
actual fun rememberBreathInput(): BreathInput {
    val input = remember { IosBreathInput() }
    DisposableEffect(input) { onDispose { input.stop() } }
    return input
}

private class IosBreathInput : BreathInput {
    override suspend fun requestPermission(): Boolean = IosBreathBridge.requestPermission()

    override fun start(
        onStrengthChanged: (Float) -> Unit,
        onError: (BreathInputError) -> Unit,
    ) = IosBreathBridge.start(onStrengthChanged, onError)

    override fun stop() = IosBreathBridge.stop()
}
