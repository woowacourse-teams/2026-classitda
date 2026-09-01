package com.pheeeew.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pheeeew.core.permission.LocationPermissionStatus
import com.pheeeew.di.LocationDependencies
import com.pheeeew.domain.model.location.LocationState
import com.pheeeew.feature.map.map.MapCameraCommand
import com.pheeeew.feature.map.map.MapDarkStyle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapScreenUiState(
    val mapState: MapUiState,
    val cameraCommand: MapCameraCommand?,
    val isRequestingLocation: Boolean,
    val isLocationAvailable: Boolean,
)

class MapViewModel(
    private val locationDependencies: LocationDependencies?,
) : ViewModel() {
    private var nextCameraCommandId = 0L

    private val _uiState =
        MutableStateFlow(
            MapScreenUiState(
                mapState = mapState(LocationState.Loading),
                cameraCommand = null,
                isRequestingLocation = false,
                isLocationAvailable = locationDependencies != null,
            ),
        )
    val uiState: StateFlow<MapScreenUiState> = _uiState.asStateFlow()

    init {
        locationDependencies?.let { dependencies ->
            viewModelScope.launch {
                dependencies.repository.locationState.collect { locationState ->
                    _uiState.update { it.copy(mapState = mapState(locationState)) }
                }
            }
        }
    }

    fun onZoomInClick() = sendCameraCommand { id -> MapCameraCommand.ZoomBy(id = id, delta = 1.0) }

    fun onZoomOutClick() = sendCameraCommand { id -> MapCameraCommand.ZoomBy(id = id, delta = -1.0) }

    fun onMyLocationClick() {
        val dependencies = locationDependencies ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRequestingLocation = true) }
            try {
                val currentStatus = dependencies.permissionController.currentStatus()
                if (currentStatus != LocationPermissionStatus.Granted) {
                    dependencies.permissionController.requestPermission()
                }
                dependencies.repository.refreshCurrentLocation()
                if (dependencies.repository.locationState.value is LocationState.Available) {
                    sendCameraCommand { id ->
                        MapCameraCommand.MoveToCurrentLocation(id = id, zoom = MapDarkStyle.FOCUS_ZOOM)
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                // 위치나 권한 값은 로그에 남기지 않습니다. 지도는 현재 카메라를 유지합니다.
            } finally {
                _uiState.update { it.copy(isRequestingLocation = false) }
            }
        }
    }

    private fun sendCameraCommand(create: (Long) -> MapCameraCommand) {
        nextCameraCommandId += 1L
        _uiState.update { it.copy(cameraCommand = create(nextCameraCommandId)) }
    }

    private fun mapState(locationState: LocationState) =
        MapUiState(
            currentLocation = (locationState as? LocationState.Available)?.location,
            locationState = locationState,
            fallbackCenter = DEFAULT_MAP_POINT,
            // 지도 핀 렌더링 확인을 위한 임시 샘플 1개입니다.
            sighMarkers = PREVIEW_SIGH_MARKERS,
            focusRequest = null,
        )
}

private val DEFAULT_MAP_POINT =
    MapPoint(
        id = "default-location",
        latitude = 37.5505,
        longitude = 127.0373,
    )

private val PREVIEW_SIGH_MARKERS =
    listOf(
        SighMarker(
            id = "preview-sigh",
            latitude = DEFAULT_MAP_POINT.latitude,
            longitude = DEFAULT_MAP_POINT.longitude,
        ),
    )
