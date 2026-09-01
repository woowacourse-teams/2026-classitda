package com.pheeeew.feature.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pheeeew.core.designsystem.theme.AppTheme
import kotlinx.coroutines.delay

private const val HOLD_AFTER_FULL_TEXT_MILLIS = 1200L

@Composable
fun TypewriterText(
    fullText: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    charDelayMillis: Long = 80L,
) {
    var visibleCharCount by remember { mutableStateOf(0) }

    LaunchedEffect(fullText) {
        while (true) {
            visibleCharCount = 0
            for (index in fullText.indices) {
                delay(charDelayMillis)
                visibleCharCount = index + 1
            }
            delay(HOLD_AFTER_FULL_TEXT_MILLIS)
        }
    }

    Text(
        text = fullText.take(visibleCharCount),
        modifier = modifier,
        style = style,
        color = color,
    )
}

@Preview
@Composable
private fun TypewriterTextPreview() {
    AppTheme {
        Box(modifier = Modifier.background(AppTheme.colors.background).padding(24.dp)) {
            TypewriterText(
                fullText = "다같이 소리질러 한숨 야호오~",
                style = AppTheme.typography.caption,
                color = AppTheme.colors.onSurfaceVariant,
            )
        }
    }
}
