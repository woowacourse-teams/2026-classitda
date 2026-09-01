package com.pheeeew.feature.map.map

import com.pheeeew.domain.model.location.CurrentLocation
import com.pheeeew.domain.model.location.LocationState
import com.pheeeew.feature.map.MapPoint
import com.pheeeew.feature.map.MapUiState
import com.pheeeew.feature.map.SighMarker

internal object MapRenderRules {
    fun renderableSighMarkers(markers: List<SighMarker>): List<SighMarker> =
        markers
            .asSequence()
            .filter { it.hasValidCoordinate() }
            .distinctBy(SighMarker::id)
            .toList()

    fun currentLocation(state: MapUiState): CurrentLocation? {
        val available = state.locationState as? LocationState.Available ?: return null
        val location = state.currentLocation ?: return null

        return location.takeIf {
            it == available.location &&
                it.hasValidCoordinate() &&
                it.accuracyMeters.isFinite() &&
                it.accuracyMeters >= 0f
        }
    }

    fun initialCenter(state: MapUiState): MapPoint? {
        currentLocation(state)?.let { location ->
            return MapPoint(
                id = "current-location",
                latitude = location.latitude,
                longitude = location.longitude,
            )
        }

        return state.fallbackCenter?.takeIf { it.hasValidCoordinate() }
    }

    fun initialCenterIsProvisional(state: MapUiState): Boolean =
        state.locationState is LocationState.Loading && initialCenter(state) != null

    private fun CurrentLocation.hasValidCoordinate(): Boolean =
        latitude.isFinite() &&
            longitude.isFinite() &&
            latitude in -90.0..90.0 &&
            longitude in -180.0..180.0

    private fun SighMarker.hasValidCoordinate(): Boolean =
        latitude.isFinite() &&
            longitude.isFinite() &&
            latitude in -90.0..90.0 &&
            longitude in -180.0..180.0

    private fun MapPoint.hasValidCoordinate(): Boolean =
        latitude.isFinite() &&
            longitude.isFinite() &&
            latitude in -90.0..90.0 &&
            longitude in -180.0..180.0
}
