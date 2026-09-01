package com.pheeeew

import androidx.compose.ui.window.ComposeUIViewController
import com.pheeeew.di.createIosLocationDependencies
import platform.UIKit.UIViewController

@Suppress("ktlint:standard:function-naming")
fun MainViewController(): UIViewController {
    val locationDependencies = createIosLocationDependencies()
    return ComposeUIViewController { App(locationDependencies) }
}
