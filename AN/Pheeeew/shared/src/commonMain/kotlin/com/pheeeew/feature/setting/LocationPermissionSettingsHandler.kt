package com.pheeeew.feature.setting

import com.pheeeew.core.permission.LocationPermissionController
import com.pheeeew.core.permission.LocationPermissionSettingsLauncher
import com.pheeeew.core.permission.LocationPermissionStatus

internal suspend fun handleLocationPermissionSettingsClick(
    permissionController: LocationPermissionController,
    settingsLauncher: LocationPermissionSettingsLauncher,
) {
    when (permissionController.currentStatus()) {
        LocationPermissionStatus.Denied -> {
            permissionController.requestPermission()
        }

        LocationPermissionStatus.ServicesDisabled -> {
            settingsLauncher.openLocationSettings()
        }

        LocationPermissionStatus.Granted,
        LocationPermissionStatus.PermanentlyDenied,
        -> {
            settingsLauncher.openAppSettings()
        }
    }
}
