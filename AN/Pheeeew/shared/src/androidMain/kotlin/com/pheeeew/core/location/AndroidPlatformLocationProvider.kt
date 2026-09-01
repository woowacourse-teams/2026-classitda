package com.pheeeew.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import androidx.core.content.ContextCompat
import com.pheeeew.domain.model.location.CurrentLocation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Android의 [LocationManager]에서 단발성 포그라운드 위치를 가져옵니다. */
class AndroidPlatformLocationProvider(
    context: Context,
    private val locationManager: LocationManager =
        requireNotNull(context.applicationContext.getSystemService(LocationManager::class.java)),
) : PlatformLocationProvider {
    private val applicationContext = context.applicationContext

    override suspend fun getCurrentLocation(): PlatformLocationResult {
        val provider = selectEnabledProvider() ?: return PlatformLocationResult.GpsUnavailable
        return requestLocation(provider)
    }

    private fun selectEnabledProvider(): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && !locationManager.isLocationEnabled) {
            return null
        }

        val hasFinePermission = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarsePermission = hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (!hasFinePermission && !hasCoarsePermission) {
            return null
        }

        if (hasFinePermission && isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER
        }

        return LocationManager.NETWORK_PROVIDER.takeIf {
            isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    private fun isProviderEnabled(provider: String): Boolean =
        try {
            locationManager.isProviderEnabled(provider)
        } catch (_: IllegalArgumentException) {
            false
        }

    @SuppressLint("MissingPermission")
    private suspend fun requestLocation(provider: String): PlatformLocationResult =
        suspendCancellableCoroutine { continuation ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val cancellationSignal = CancellationSignal()
                continuation.invokeOnCancellation { cancellationSignal.cancel() }

                try {
                    locationManager.getCurrentLocation(
                        provider,
                        cancellationSignal,
                        ContextCompat.getMainExecutor(applicationContext),
                    ) { location ->
                        if (continuation.isActive) {
                            continuation.resume(location.toPlatformResult())
                        }
                    }
                } catch (_: SecurityException) {
                    if (continuation.isActive) {
                        continuation.resume(PlatformLocationResult.GpsUnavailable)
                    }
                } catch (_: IllegalArgumentException) {
                    if (continuation.isActive) {
                        continuation.resume(PlatformLocationResult.GpsUnavailable)
                    }
                }
            } else {
                requestLegacyLocation(provider, continuation)
            }
        }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    @SuppressLint("MissingPermission")
    private fun requestLegacyLocation(
        provider: String,
        continuation: kotlinx.coroutines.CancellableContinuation<PlatformLocationResult>,
    ) {
        val listener =
            object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    if (continuation.isActive) {
                        continuation.resume(location.toPlatformResult())
                    }
                }

                override fun onProviderDisabled(disabledProvider: String) {
                    if (disabledProvider != provider) return
                    locationManager.removeUpdates(this)
                    if (continuation.isActive) {
                        continuation.resume(PlatformLocationResult.GpsUnavailable)
                    }
                }

                override fun onProviderEnabled(enabledProvider: String) = Unit

                override fun onStatusChanged(
                    changedProvider: String?,
                    status: Int,
                    extras: Bundle?,
                ) = Unit
            }

        continuation.invokeOnCancellation {
            locationManager.removeUpdates(listener)
        }

        try {
            locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
        } catch (_: SecurityException) {
            if (continuation.isActive) {
                continuation.resume(PlatformLocationResult.GpsUnavailable)
            }
        } catch (_: IllegalArgumentException) {
            if (continuation.isActive) {
                continuation.resume(PlatformLocationResult.GpsUnavailable)
            }
        }
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(applicationContext, permission) ==
            PackageManager.PERMISSION_GRANTED

    private fun Location?.toPlatformResult(): PlatformLocationResult {
        this ?: return PlatformLocationResult.GpsUnavailable
        return androidPlatformLocationResult(
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = accuracy.takeIf { hasAccuracy() },
            capturedAtMillis = time,
        )
    }
}

internal fun androidPlatformLocationResult(
    latitude: Double,
    longitude: Double,
    accuracyMeters: Float?,
    capturedAtMillis: Long,
): PlatformLocationResult {
    if (
        !latitude.isFinite() ||
        !longitude.isFinite() ||
        latitude !in -90.0..90.0 ||
        longitude !in -180.0..180.0 ||
        accuracyMeters == null ||
        !accuracyMeters.isFinite() ||
        accuracyMeters < 0f ||
        capturedAtMillis <= 0L
    ) {
        return PlatformLocationResult.GpsUnavailable
    }

    return PlatformLocationResult.Success(
        CurrentLocation(
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = accuracyMeters,
            capturedAtMillis = capturedAtMillis,
        ),
    )
}
