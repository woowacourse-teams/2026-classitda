package com.pheeeew.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pheeeew.core.designsystem.theme.AppColors
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

    Column(
        modifier = modifier.fillMaxSize().background(AppTheme.colors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.fillMaxHeight(0.35f))

        Image(
            painter = painterResource(Res.drawable.logo_pheeeew),
            contentDescription = "Pheeeew",
            modifier = Modifier.width(220.dp),
            contentScale = ContentScale.FillWidth,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "아우 힘들다~ 너도? 나도~",
            style = AppTheme.typography.menuItem,
            color = AppColors.Cream100,
        )

        Spacer(modifier = Modifier.weight(1f))

        TypewriterText(
            fullText = "다같이 소리질러 한숨 야호오~",
            style = AppTheme.typography.menuItem,
            color = AppColors.Cream100,
        )

        Spacer(modifier = Modifier.height(64.dp))
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

private const val SPLASH_MINIMUM_DISPLAY_MILLIS = 2500L
private const val SPLASH_MAXIMUM_DISPLAY_MILLIS = 8000L

@Preview
@Composable
private fun SplashScreenPreview() {
    AppTheme {
        SplashScreen(isReady = flowOf(true), onFinished = {})
    }
}
