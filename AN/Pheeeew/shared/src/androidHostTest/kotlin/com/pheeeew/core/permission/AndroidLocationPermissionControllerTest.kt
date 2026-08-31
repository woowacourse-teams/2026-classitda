package com.pheeeew.core.permission

import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidLocationPermissionControllerTest {
    @Test
    fun grantedPermissionWinsRegardlessOfRationaleState() {
        val status =
            androidLocationPermissionStatus(
                hasPermission = true,
                wasRequested = true,
                isActivityAttached = false,
                canExplainDenial = false,
            )

        assertEquals(LocationPermissionStatus.Granted, status)
    }

    @Test
    fun detachedActivityDoesNotProduceFalsePermanentDenial() {
        val status =
            androidLocationPermissionStatus(
                hasPermission = false,
                wasRequested = true,
                isActivityAttached = false,
                canExplainDenial = false,
            )

        assertEquals(LocationPermissionStatus.Denied, status)
    }

    @Test
    fun requestedPermissionWithoutRationaleIsPermanentlyDenied() {
        val status =
            androidLocationPermissionStatus(
                hasPermission = false,
                wasRequested = true,
                isActivityAttached = true,
                canExplainDenial = false,
            )

        assertEquals(LocationPermissionStatus.PermanentlyDenied, status)
    }
}
