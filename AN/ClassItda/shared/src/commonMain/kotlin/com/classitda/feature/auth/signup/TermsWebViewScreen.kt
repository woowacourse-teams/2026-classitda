package com.classitda.feature.auth.signup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.component.NavigateBackTopBar
import com.classitda.core.platform.PlatformWebView

@Composable
internal fun TermsWebViewScreen(
    title: String,
    url: String,
    onNavigateBack: () -> Unit,
) {
    var isLoading by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        NavigateBackTopBar(
            title = title,
            onNavigateBack = onNavigateBack,
        )
        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            PlatformWebView(
                url = url,
                onLoadingChanged = { isLoading = it },
                modifier = Modifier.fillMaxSize(),
            )
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = StuColors.Gray900,
                )
            }
        }
    }
}
