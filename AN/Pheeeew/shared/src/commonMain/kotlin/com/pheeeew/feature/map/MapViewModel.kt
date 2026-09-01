package com.pheeeew.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pheeeew.core.permission.LocationPermissionStatus
import com.pheeeew.di.LocationDependencies
import com.pheeeew.domain.exception.ApiException
import com.pheeeew.domain.model.location.LocationState
import com.pheeeew.domain.repository.SighRepository
import com.pheeeew.feature.map.map.MapCameraCommand
import com.pheeeew.feature.map.map.MapDarkStyle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class MapViewModel(
    private val sighRepository: SighRepository,
    private val locationDependencies: LocationDependencies?,
) : ViewModel() {
    private var nextCameraCommandId = 0L
    private var pendingRequestId: String? = null

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    // 스플래시 화면 노출 시간 설정을 위한 플로우
    val isReady: Flow<Boolean> = uiState.map { it !is MapUiState.Loading }

    private val _sighReleasedEvents = Channel<Unit>(Channel.BUFFERED)
    val sighReleasedEvents: Flow<Unit> = _sighReleasedEvents.receiveAsFlow()

    init {
        loadSighs()
        locationDependencies?.let { dependencies ->
            viewModelScope.launch {
                dependencies.repository.locationState.collect { locationState ->
                    _uiState.update { state ->
                        (state as? MapUiState.Success)?.copy(locationState = locationState) ?: state
                    }
                }
            }
            viewModelScope.launch {
                try {
                    dependencies.permissionController.requestPermission()
                    dependencies.repository.refreshCurrentLocation()
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    // 위치나 권한 값은 로그에 남기지 않습니다.
                }
            }
        }
    }

    fun loadSighs() {
        viewModelScope.launch {
            _uiState.value = MapUiState.Loading
            try {
                val sighs = sighRepository.getSighs()
                _uiState.value =
                    MapUiState.Success(
                        sighs = sighs,
                        locationState = locationDependencies?.repository?.locationState?.value ?: LocationState.Loading,
                        cameraCommand = null,
                        isRequestingLocation = false,
                    )
            } catch (e: ApiException) {
                _uiState.value = MapUiState.Error(e.message)
            }
        }
    }

    fun sendSigh() {
        val current = _uiState.value as? MapUiState.Success ?: return
        val location = (current.locationState as? LocationState.Available)?.location

        if (location == null) {
            val message =
                (current.locationState as? LocationState.Unavailable)?.reason?.toKoreanMessage()
                    ?: "GPS 수신이 원활하지 않습니다."
            _uiState.value = current.copy(sighReleaseState = SighReleaseState.Error(message))
            return
        }

        val requestId = pendingRequestId ?: Uuid.random().toString().also { pendingRequestId = it }

        viewModelScope.launch {
            _uiState.value = current.copy(sighReleaseState = SighReleaseState.Submitting)
            try {
                val sighPin = sighRepository.registerSigh(requestId, location.coordinate)
                pendingRequestId = null
                _uiState.value =
                    current.copy(
                        sighs = current.sighs + sighPin,
                        sighReleaseState = SighReleaseState.Idle,
                    )
                _sighReleasedEvents.send(Unit)
            } catch (e: ApiException) {
                _uiState.value = current.copy(sighReleaseState = SighReleaseState.Error(e.message))
            }
        }
    }

    fun cancelSighRelease() {
        pendingRequestId = null
        val current = _uiState.value as? MapUiState.Success ?: return
        _uiState.value = current.copy(sighReleaseState = SighReleaseState.Idle)
    }

    fun onZoomInClick() = sendCameraCommand { id -> MapCameraCommand.ZoomBy(id = id, delta = 1.0) }

    fun onZoomOutClick() = sendCameraCommand { id -> MapCameraCommand.ZoomBy(id = id, delta = -1.0) }

    fun onMyLocationClick() {
        val dependencies = locationDependencies ?: return
        val current = _uiState.value as? MapUiState.Success ?: return
        if (current.isRequestingLocation) return

        viewModelScope.launch {
            _uiState.update { (it as? MapUiState.Success)?.copy(isRequestingLocation = true) ?: it }
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
                _uiState.update { (it as? MapUiState.Success)?.copy(isRequestingLocation = false) ?: it }
            }
        }
    }

    fun openLocationSettings() {
        locationDependencies?.permissionController?.openAppSettings()
    }

    private fun sendCameraCommand(create: (Long) -> MapCameraCommand) {
        nextCameraCommandId += 1L
        _uiState.update { state ->
            (state as? MapUiState.Success)?.copy(cameraCommand = create(nextCameraCommandId)) ?: state
        }
    }
}
