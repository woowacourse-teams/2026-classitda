package com.pheeeew

import androidx.compose.ui.window.ComposeUIViewController
import com.pheeeew.data.repository.FakeSighRepository
import com.pheeeew.di.createIosLocationDependencies
import platform.Foundation.NSBundle

@Suppress("ktlint:standard:function-naming")
fun MainViewController() =
    ComposeUIViewController {
        val appVersion = NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "-"
        val locationDependencies = createIosLocationDependencies()
        App(
            appVersion = appVersion,
            locationDependencies = locationDependencies,
            sighRepository = FakeSighRepository(),
        )
    }
