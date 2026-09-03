package com.pheeeew.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pheeeew.core.designsystem.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.painterResource
import pheeeew.shared.generated.resources.Res
import pheeeew.shared.generated.resources.logo_pheeeew

@Composable
fun SplashScreen(
    isReady: Flow<Boolean>,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(isReady) {
        joinAll(
            launch { delay(SPLASH_MINIMUM_DISPLAY_MILLIS) },
            launch { withTimeoutOrNull(SPLASH_MAXIMUM_DISPLAY_MILLIS) { isReady.first { it } } },
        )
        onFinished()
    }

    Box(
        modifier = modifier.fillMaxSize().background(AppTheme.colors.background),
    ) {
        TwinklingStars(modifier = Modifier.align(Alignment.TopCenter))

        val logoAlpha = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = LOGO_FADE_IN_MILLIS),
            )
        }

        Image(
            painter = painterResource(Res.drawable.logo_pheeeew),
            contentDescription = "Pheeeew",
            modifier = Modifier.width(220.dp).align(BiasAlignment(0f, LOGO_VERTICAL_BIAS)).alpha(logoAlpha.value),
            contentScale = ContentScale.FillWidth,
        )
    }
}

private const val SPLASH_MINIMUM_DISPLAY_MILLIS = 3500L
private const val SPLASH_MAXIMUM_DISPLAY_MILLIS = 8000L
private const val LOGO_FADE_IN_MILLIS = 2400
private const val LOGO_VERTICAL_BIAS = -0.15f

@Preview
@Composable
private fun SplashScreenPreview() {
    AppTheme {
        SplashScreen(isReady = flowOf(true), onFinished = {})
    }
}
