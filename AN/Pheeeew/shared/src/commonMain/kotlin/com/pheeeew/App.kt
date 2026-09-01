package com.pheeeew

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pheeeew.core.designsystem.theme.AppTheme
import com.pheeeew.core.navigation.Screen
import com.pheeeew.di.LocationDependencies
import com.pheeeew.feature.map.TempMapScreen
import com.pheeeew.feature.setting.SettingsScreen
import com.pheeeew.feature.setting.legal.LegalDocument
import com.pheeeew.feature.setting.legal.LegalDocumentRoute
import com.pheeeew.feature.splash.SplashScreen

@Composable
fun App(
    appVersion: String,
    locationDependencies: LocationDependencies?,
) {
    AppTheme {
        var screen by remember { mutableStateOf(Screen.Splash) }
        var selectedLegalDocument by remember { mutableStateOf<LegalDocument?>(null) }
        Box(modifier = Modifier.fillMaxSize().background(AppTheme.colors.background)) {
            when (screen) {
                Screen.Splash -> {
                    SplashScreen(
                        onFinished = { screen = Screen.Map },
                    )
                }

                Screen.Map -> {
                    TempMapScreen(
                        onSettingsClick = { screen = Screen.Settings },
                    )
                }

                Screen.Settings -> {
                    SettingsScreen(
                        onBackClick = { screen = Screen.Map },
                        onThemeSettingClick = {},
                        onLocationPermissionClick = {},
                        onContactClick = {},
                        onOpenSourceLicenseClick = {
                            selectedLegalDocument = LegalDocument.OpenSourceLicenses
                            screen = Screen.LegalDocument
                        },
                        onPrivacyPolicyClick = {
                            selectedLegalDocument = LegalDocument.PrivacyPolicy
                            screen = Screen.LegalDocument
                        },
                        onLocationPolicyClick = {},
                        onCreditsClick = {},
                        appVersion = appVersion,
                    )
                }

                Screen.LegalDocument -> {
                    selectedLegalDocument?.let { document ->
                        LegalDocumentRoute(
                            document = document,
                            onBack = { screen = Screen.Settings },
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AppPreview() {
    App(appVersion = "1.0.0", locationDependencies = null)
}
