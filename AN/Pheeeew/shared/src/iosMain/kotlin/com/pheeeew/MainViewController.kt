package com.pheeeew

import androidx.compose.ui.window.ComposeUIViewController
import platform.Foundation.NSBundle

@Suppress("ktlint:standard:function-naming")
fun MainViewController() =
    ComposeUIViewController {
        val appVersion = NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "-"
        App(appVersion = appVersion)
    }
    
// TODO: 충돌 해결을 위한 임시 주석 처리
// import com.pheeeew.di.createIosLocationDependencies
// import platform.UIKit.UIViewController
//
// @Suppress("ktlint:standard:function-naming")
// fun MainViewController(): UIViewController {
//     val locationDependencies = createIosLocationDependencies()
//     return ComposeUIViewController { App(locationDependencies) }
// }
