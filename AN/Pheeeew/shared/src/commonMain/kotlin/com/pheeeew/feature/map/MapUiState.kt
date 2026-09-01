package com.pheeeew.feature.map

import com.pheeeew.domain.model.location.CurrentLocation
import com.pheeeew.domain.model.location.LocationState

data class MapPoint(
    val id: String,
    val latitude: Double,
    val longitude: Double,
)

data class SighMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
)

data class MapFocusRequest(
    val id: String,
    val latitude: Double,
    val longitude: Double,
)

data class MapUiState(
    val currentLocation: CurrentLocation?,
    val locationState: LocationState,
    val fallbackCenter: MapPoint?,
    val sighMarkers: List<SighMarker>,
    val focusRequest: MapFocusRequest?,
)
