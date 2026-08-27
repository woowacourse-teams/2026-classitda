package com.classitda

import androidx.compose.ui.window.ComposeUIViewController
import com.classitda.core.auth.IosWithdrawalStateStorage
import com.classitda.core.auth.SettingsAuthTokenStorage
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import kotlinx.cinterop.ExperimentalForeignApi

@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalForeignApi::class, ExperimentalSettingsImplementation::class)
fun MainViewController() =
    ComposeUIViewController {
        App(
            tokenStorage = SettingsAuthTokenStorage(KeychainSettings.Factory().create("auth_tokens")),
            withdrawalStateStorage = IosWithdrawalStateStorage(),
        )
    }
