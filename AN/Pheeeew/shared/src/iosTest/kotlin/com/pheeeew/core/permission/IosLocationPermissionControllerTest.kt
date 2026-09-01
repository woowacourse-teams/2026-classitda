package com.pheeeew.core.permission

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalForeignApi::class)
class IosLocationPermissionControllerTest {
    @Test
    fun authorizedWhenInUseMapsToGranted() {
        assertEquals(
            LocationPermissionStatus.Granted,
            kCLAuthorizationStatusAuthorizedWhenInUse.toCommonStatus(),
        )
    }

    @Test
    fun deniedMapsToPermanentlyDenied() {
        assertEquals(
            LocationPermissionStatus.PermanentlyDenied,
            kCLAuthorizationStatusDenied.toCommonStatus(locationServicesEnabled = true),
        )
    }

    @Test
    fun disabledLocationServicesPassPermissionGateForGpsErrorClassification() {
        assertEquals(
            LocationPermissionStatus.Granted,
            kCLAuthorizationStatusDenied.toCommonStatus(locationServicesEnabled = false),
        )
    }

    @Test
    fun notDeterminedMapsToDenied() {
        assertEquals(
            LocationPermissionStatus.Denied,
            kCLAuthorizationStatusNotDetermined.toCommonStatus(),
        )
    }
}
