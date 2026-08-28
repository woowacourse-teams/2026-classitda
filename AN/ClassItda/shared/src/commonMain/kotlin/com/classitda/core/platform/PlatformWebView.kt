package com.classitda.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformWebView(
    url: String,
    onLoadingChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
)
