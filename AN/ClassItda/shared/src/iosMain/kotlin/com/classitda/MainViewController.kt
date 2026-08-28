package com.classitda

import androidx.compose.ui.window.ComposeUIViewController
import com.classitda.core.auth.SettingsAuthTokenStorage
import com.classitda.core.database.createPlatformDatabaseModule
import com.classitda.core.network.ClassItdaApiConfig
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalForeignApi::class, ExperimentalSettingsImplementation::class, ExperimentalNativeApi::class)
fun MainViewController() =
    createPlatformDatabaseModule().let { localDatabaseModule ->
        ComposeUIViewController {
            App(
                baseUrl =
                    if (Platform.isDebugBinary) {
                        ClassItdaApiConfig.DEV_BASE_URL
                    } else {
                        ClassItdaApiConfig.PROD_BASE_URL
                    },
                localDatabaseModule = localDatabaseModule,
                tokenStorage = SettingsAuthTokenStorage(KeychainSettings.Factory().create("auth_tokens")),
            )
        }
    }
