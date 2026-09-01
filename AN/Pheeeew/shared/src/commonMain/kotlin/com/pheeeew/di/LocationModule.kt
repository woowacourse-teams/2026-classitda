package com.pheeeew.di

import com.pheeeew.core.location.LocationFreshnessPolicy
import com.pheeeew.core.location.PlatformLocationProvider
import com.pheeeew.core.permission.LocationPermissionController
import com.pheeeew.data.repository.LocationRepositoryImpl
import com.pheeeew.domain.repository.LocationRepository

/** 지도와 등록 흐름이 반드시 같은 위치 Repository를 공유하도록 묶은 의존성입니다. */
data class LocationDependencies(
    val permissionController: LocationPermissionController,
    val repository: LocationRepository,
)

object LocationModule {
    fun create(
        permissionController: LocationPermissionController,
        locationProvider: PlatformLocationProvider,
        freshnessPolicy: LocationFreshnessPolicy = LocationFreshnessPolicy(),
        locationTimeoutMillis: Long = LocationRepositoryImpl.DEFAULT_LOCATION_TIMEOUT_MILLIS,
    ): LocationDependencies =
        LocationDependencies(
            permissionController = permissionController,
            repository =
                LocationRepositoryImpl(
                    permissionController = permissionController,
                    locationProvider = locationProvider,
                    freshnessPolicy = freshnessPolicy,
                    locationTimeoutMillis = locationTimeoutMillis,
                ),
        )
}
