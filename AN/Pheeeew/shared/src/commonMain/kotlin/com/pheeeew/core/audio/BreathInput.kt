package com.pheeeew.core.audio

import androidx.compose.runtime.Composable

interface BreathInput {
    fun start(
        onStrengthChanged: (Float) -> Unit,
        onError: (BreathInputError) -> Unit,
    )

    fun stop()
}

@Composable
expect fun rememberBreathInput(): BreathInput
