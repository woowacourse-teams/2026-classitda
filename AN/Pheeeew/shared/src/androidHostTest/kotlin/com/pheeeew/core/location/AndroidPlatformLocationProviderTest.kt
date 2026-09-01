package com.pheeeew.core.location

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class AndroidPlatformLocationProviderTest {
    @Test
    fun validNativeValuesBecomeCurrentLocation() {
        val result =
            androidPlatformLocationResult(
                latitude = 37.5505,
                longitude = 127.0373,
                accuracyMeters = 7.5f,
                capturedAtMillis = 1_000L,
            )

        val success = assertIs<PlatformLocationResult.Success>(result)
        assertEquals(37.5505, success.location.latitude)
        assertEquals(127.0373, success.location.longitude)
        assertEquals(7.5f, success.location.accuracyMeters)
        assertEquals(1_000L, success.location.capturedAtMillis)
    }

    @Test
    fun missingAccuracyIsRejected() {
        val result =
            androidPlatformLocationResult(
                latitude = 37.5505,
                longitude = 127.0373,
                accuracyMeters = null,
                capturedAtMillis = 1_000L,
            )

        assertSame(PlatformLocationResult.GpsUnavailable, result)
    }

    @Test
    fun invalidCoordinateOrTimestampIsRejected() {
        val invalidCoordinate =
            androidPlatformLocationResult(91.0, 127.0373, 7.5f, 1_000L)
        val invalidTimestamp =
            androidPlatformLocationResult(37.5505, 127.0373, 7.5f, 0L)

        assertSame(PlatformLocationResult.GpsUnavailable, invalidCoordinate)
        assertSame(PlatformLocationResult.GpsUnavailable, invalidTimestamp)
    }
}
