package com.classitda.feature.common.privacypolicy

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal expect fun PrivacyPolicyWebContent(
    reloadToken: Int,
    onLoadingChanged: (Boolean) -> Unit,
    onLoadFailed: (PrivacyPolicyError) -> Unit,
    onNavigationBlocked: () -> Unit,
    modifier: Modifier,
)
