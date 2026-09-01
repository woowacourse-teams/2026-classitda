package com.pheeeew.core.permission

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import kotlin.coroutines.resume

class IosLocationPermissionSettingsLauncher : LocationPermissionSettingsLauncher {
    override suspend fun openAppSettings(): Boolean =
        withContext(Dispatchers.Main) {
            val settingsUrl = NSURL(string = UIApplicationOpenSettingsURLString)
            val application = UIApplication.sharedApplication

            suspendCancellableCoroutine { continuation ->
                application.openURL(
                    settingsUrl,
                    options = emptyMap<Any?, Any>(),
                    completionHandler = { opened ->
                        if (continuation.isActive) continuation.resume(opened)
                    },
                )
            }
        }
}
