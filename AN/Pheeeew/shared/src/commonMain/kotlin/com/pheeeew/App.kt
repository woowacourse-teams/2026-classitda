package com.pheeeew

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pheeeew.core.designsystem.theme.AppTheme
import com.pheeeew.core.navigation.Screen
import com.pheeeew.data.repository.FakeSighRepository
import com.pheeeew.di.LocationDependencies
import com.pheeeew.feature.map.MapScreen
import com.pheeeew.feature.map.MapViewModel
import com.pheeeew.feature.setting.SettingsScreen
import com.pheeeew.feature.setting.handleLocationPermissionSettingsClick
import com.pheeeew.feature.setting.legal.LegalDocument
import com.pheeeew.feature.setting.legal.LegalDocumentRoute
import com.pheeeew.feature.splash.SplashScreen
import kotlinx.coroutines.launch

@Composable
fun App(
    appVersion: String,
    locationDependencies: LocationDependencies?,
) {
    AppTheme {
        val coroutineScope = rememberCoroutineScope()
        var screen by remember { mutableStateOf(Screen.Splash) }
        val mapViewModel: MapViewModel = viewModel { MapViewModel(FakeSighRepository(), locationDependencies) }
        var selectedLegalDocument by remember { mutableStateOf<LegalDocument?>(null) }

        Box(modifier = Modifier.fillMaxSize().background(AppTheme.colors.background)) {
            when (screen) {
                Screen.Splash -> {
                    SplashScreen(
                        isReady = mapViewModel.isReady,
                        onFinished = { screen = Screen.Map },
                    )
                }

                Screen.Map -> {
                    MapScreen(
                        locationDependencies = locationDependencies,
                        onSettingsClick = { screen = Screen.Settings },
                        viewModel = mapViewModel,
                    )
                }

                Screen.Settings -> {
                    SettingsScreen(
                        onBackClick = { screen = Screen.Map },
                        onThemeSettingClick = {},
                        onLocationPermissionClick = {
                            locationDependencies?.let { dependencies ->
                                coroutineScope.launch {
                                    handleLocationPermissionSettingsClick(
                                        permissionController = dependencies.permissionController,
                                        settingsLauncher = dependencies.permissionSettingsLauncher,
                                    )
                                }
                            }
                        },
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
