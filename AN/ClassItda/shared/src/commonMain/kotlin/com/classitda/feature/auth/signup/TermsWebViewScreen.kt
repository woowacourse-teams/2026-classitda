package com.classitda.feature.auth.signup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.classitda.core.designsystem.component.NavigateBackTopBar
import com.classitda.core.platform.PlatformWebView

@Composable
internal fun TermsWebViewScreen(
    title: String,
    url: String,
    onNavigateBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        NavigateBackTopBar(
            title = title,
            onNavigateBack = onNavigateBack,
        )
        PlatformWebView(
            url = url,
            modifier = Modifier.fillMaxSize().weight(1f),
        )
    }
}
