package com.pheeeew.di

import com.pheeeew.core.location.IosPlatformLocationProvider
import com.pheeeew.core.permission.IosLocationPermissionController

fun createIosLocationDependencies(): LocationDependencies =
    LocationModule.create(
        permissionController = IosLocationPermissionController(),
        locationProvider = IosPlatformLocationProvider(),
    )
