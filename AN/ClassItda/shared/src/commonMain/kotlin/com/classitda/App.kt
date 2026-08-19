package com.classitda

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.auth.signup.SignupRoute

@Composable
@Preview
fun App() {
    AppTheme(theme = ThemeType.STUDENT) {
        SignupRoute()
    }
}
