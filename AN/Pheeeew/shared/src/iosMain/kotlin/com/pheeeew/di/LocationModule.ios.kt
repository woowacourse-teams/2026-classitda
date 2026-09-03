package com.pheeeew.di

import com.pheeeew.core.location.IosPlatformLocationProvider
import com.pheeeew.core.permission.IosLocationPermissionController
import com.pheeeew.core.permission.IosLocationPermissionSettingsLauncher

fun createIosLocationDependencies(): LocationDependencies =
    LocationModule.create(
        permissionController = IosLocationPermissionController(),
        permissionSettingsLauncher = IosLocationPermissionSettingsLauncher(),
        locationProvider = IosPlatformLocationProvider(),
    )
