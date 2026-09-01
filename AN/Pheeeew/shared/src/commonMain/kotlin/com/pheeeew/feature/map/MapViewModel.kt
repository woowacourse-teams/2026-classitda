package com.pheeeew.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pheeeew.core.permission.LocationPermissionStatus
import com.pheeeew.di.LocationDependencies
import com.pheeeew.domain.exception.ApiException
import com.pheeeew.domain.model.geo.Coordinate
import com.pheeeew.domain.model.location.LocationState
import com.pheeeew.domain.model.sigh.SighPin
import com.pheeeew.domain.repository.SighRepository
import com.pheeeew.feature.map.map.MapCameraCommand
import com.pheeeew.feature.map.map.MapDarkStyle
import com.pheeeew.feature.setting.handleLocationPermissionSettingsClick
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class MapViewModel(
    private val sighRepository: SighRepository,
    private val locationDependencies: LocationDependencies?,
) : ViewModel() {
    private var nextCameraCommandId = 0L
    private var pendingRegistration: PendingSighRequest? = null

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    // 스플래시 화면 노출 시간 설정을 위한 플로우
    val isReady: Flow<Boolean> = uiState.map { it !is MapUiState.Loading }

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
            val wasLoaded = _uiState.value is MapUiState.Success
            if (!wasLoaded) _uiState.value = MapUiState.Loading
            try {
                val sighs = sighRepository.getSighs()
                _uiState.update { state ->
                    if (state is MapUiState.Success) {
                        state.copy(sighs = sighs.distinctBy(SighPin::id), refreshErrorMessage = null)
                    } else {
                        MapUiState.Success(
                            sighs = sighs.distinctBy(SighPin::id),
                            locationState =
                                locationDependencies?.repository?.locationState?.value ?: LocationState.Loading,
                            cameraCommand = null,
                            isRequestingLocation = false,
                        )
                    }
                }
            } catch (e: ApiException) {
                _uiState.update { state ->
                    if (state is MapUiState.Success) {
                        state.copy(refreshErrorMessage = e.message)
                    } else {
                        MapUiState.Error(e.message)
                    }
                }
            }
        }
    }

    fun registerSighAfterExplosion() {
        val current = _uiState.value as? MapUiState.Success ?: return
        if (current.sighReleaseState is SighReleaseState.Submitting) return
        val location = (current.locationState as? LocationState.Available)?.location

        if (location == null) {
            val message =
                (current.locationState as? LocationState.Unavailable)?.reason?.toKoreanMessage()
                    ?: "GPS 수신이 원활하지 않습니다."
            _uiState.value =
                current.copy(
                    sighReleaseState = SighReleaseState.Error(message = message, canRetry = false),
                )
            return
        }

        val request =
            pendingRegistration
                ?: PendingSighRequest(
                    requestId = Uuid.random().toString(),
                    coordinate = location.coordinate,
                ).also { pendingRegistration = it }
        submit(request)
    }

    /** 이전 API 이름과의 호환용 진입점입니다. */
    fun sendSigh() = registerSighAfterExplosion()

    private fun submit(request: PendingSighRequest) {
        viewModelScope.launch {
            _uiState.update { (it as? MapUiState.Success)?.copy(sighReleaseState = SighReleaseState.Submitting) ?: it }
            try {
                val sighPin = sighRepository.registerSigh(request.requestId, request.coordinate)
                pendingRegistration = null
                _uiState.update { state ->
                    (state as? MapUiState.Success)?.copy(
                        sighs = (state.sighs + sighPin).distinctBy(SighPin::id),
                        sighReleaseState = SighReleaseState.Idle,
                        focusRequest =
                            MapFocusRequest(
                                id = sighPin.id.toString(),
                                latitude = sighPin.coordinate.latitude,
                                longitude = sighPin.coordinate.longitude,
                            ),
                    )
                        ?: state
                }
            } catch (e: ApiException) {
                _uiState.update {
                    (it as? MapUiState.Success)?.copy(
                        sighReleaseState = SighReleaseState.Error(message = e.message, canRetry = true),
                    ) ?: it
                }
            }
        }
    }

    fun retrySighRegistration() {
        pendingRegistration?.let(::submit)
    }

    fun cancelFailedSighRegistration() {
        pendingRegistration = null
        val current = _uiState.value as? MapUiState.Success ?: return
        _uiState.value = current.copy(sighReleaseState = SighReleaseState.Idle)
    }

    fun cancelSighRelease() = cancelFailedSighRegistration()

    fun consumeFocusRequest(id: String) {
        _uiState.update { state ->
            if (state is MapUiState.Success && state.focusRequest?.id == id) state.copy(focusRequest = null) else state
        }
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
        val dependencies = locationDependencies ?: return
        viewModelScope.launch {
            handleLocationPermissionSettingsClick(
                permissionController = dependencies.permissionController,
                settingsLauncher = dependencies.permissionSettingsLauncher,
            )
        }
    }

    private fun sendCameraCommand(create: (Long) -> MapCameraCommand) {
        nextCameraCommandId += 1L
        _uiState.update { state ->
            (state as? MapUiState.Success)?.copy(cameraCommand = create(nextCameraCommandId)) ?: state
        }
    }

    private data class PendingSighRequest(
        val requestId: String,
        val coordinate: Coordinate,
    )
}
