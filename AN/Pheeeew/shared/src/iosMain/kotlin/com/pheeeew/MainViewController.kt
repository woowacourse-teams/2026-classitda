package com.pheeeew

import androidx.compose.ui.window.ComposeUIViewController
import platform.Foundation.NSBundle

@Suppress("ktlint:standard:function-naming")
fun MainViewController() =
    ComposeUIViewController {
        val appVersion = NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "-"
        App(appVersion = appVersion)
    }
