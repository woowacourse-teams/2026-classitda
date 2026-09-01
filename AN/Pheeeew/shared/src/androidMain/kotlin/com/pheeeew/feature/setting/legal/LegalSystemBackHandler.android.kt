package com.pheeeew.feature.setting.legal

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
internal actual fun LegalSystemBackHandler(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
}
