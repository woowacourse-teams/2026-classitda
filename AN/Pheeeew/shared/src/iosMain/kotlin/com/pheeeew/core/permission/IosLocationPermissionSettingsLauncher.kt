package com.pheeeew.core.permission

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

class IosLocationPermissionSettingsLauncher : LocationPermissionSettingsLauncher {
    override suspend fun openAppSettings(): Boolean =
        withContext(Dispatchers.Main) {
            val settingsUrl = NSURL(string = UIApplicationOpenSettingsURLString)
            val application = UIApplication.sharedApplication
            if (!application.canOpenURL(settingsUrl)) return@withContext false

            application.openURL(
                settingsUrl,
                options = emptyMap<Any?, Any>(),
                completionHandler = null,
            )
            true
        }
}
