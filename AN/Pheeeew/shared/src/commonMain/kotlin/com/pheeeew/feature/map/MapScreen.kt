package com.pheeeew.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pheeeew.core.audio.BreathInputError
import com.pheeeew.core.designsystem.component.AppDialog
import com.pheeeew.core.designsystem.theme.AppTheme
import com.pheeeew.core.permission.LocationPermissionStatus
import com.pheeeew.data.repository.FakeSighRepository
import com.pheeeew.di.LocationDependencies
import com.pheeeew.domain.model.location.LocationState
import com.pheeeew.feature.map.animation.LandingHighlightOverlay
import com.pheeeew.feature.map.animation.SighAnimationCoordinator
import com.pheeeew.feature.map.animation.StarFlightOverlay
import com.pheeeew.feature.map.map.BreathMap
import com.pheeeew.feature.map.map.MapProjectionSnapshot
import com.pheeeew.feature.map.overlay.BreathControl
import com.pheeeew.feature.map.overlay.MapOverlay

@Composable
fun MapScreen(
    locationDependencies: LocationDependencies?,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel { MapViewModel(FakeSighRepository(), locationDependencies) },
) {
    val uiState by viewModel.uiState.collectAsState()
    val successState = uiState as? MapUiState.Success
    var pendingFlightOrigin by remember { mutableStateOf<Offset?>(null) }
    var projectionSnapshot by remember { mutableStateOf(MapProjectionSnapshot.Empty) }
    var activeFlightId by remember { mutableStateOf<String?>(null) }
    var landedFlightId by remember { mutableStateOf<String?>(null) }
    var isFlightInProgress by remember { mutableStateOf(false) }
    val highlightedFeatureIds = remember { mutableStateListOf<String>() }
    val animationCoordinator = remember { SighAnimationCoordinator() }
    var microphoneError by remember { mutableStateOf<BreathInputError?>(null) }
    var showLocationPermissionDialog by remember { mutableStateOf(false) }
    var showMicrophonePermissionDialog by remember { mutableStateOf(false) }

    val hiddenMarkerId =
        successState?.focusRequest?.id?.takeIf {
            pendingFlightOrigin != null && landedFlightId != it
        }

    LaunchedEffect(successState?.focusRequest?.id, projectionSnapshot.revision) {
        val focus = successState?.focusRequest ?: return@LaunchedEffect
        pendingFlightOrigin ?: return@LaunchedEffect
        val destination = projectionSnapshot.points[focus.id] ?: return@LaunchedEffect
        if (!projectionSnapshot.cameraIdle || activeFlightId == focus.id) return@LaunchedEffect
        activeFlightId = focus.id
        isFlightInProgress = true
    }

    LaunchedEffect(successState?.sighReleaseState) {
        val error = successState?.sighReleaseState as? SighReleaseState.Error ?: return@LaunchedEffect
        if (!error.canRetry) {
            pendingFlightOrigin = null
            viewModel.cancelFailedSighRegistration()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(AppTheme.colors.background)) {
        if (successState != null) {
            BreathMap(
                state = successState.toMapRenderState(hiddenMarkerId),
                cameraCommand = successState.cameraCommand,
                onSighClick = {},
                onBoundsChanged = viewModel::loadSighs,
                onMapError = {},
                onProjectionChanged = { projectionSnapshot = it },
                modifier = Modifier.fillMaxSize(),
            )
        }

        MapOverlay(
            onSettingsClick = onSettingsClick,
            onRefreshClick = viewModel::loadSighs,
            breathControl = {
                BreathControl(
                    enabled =
                        successState != null &&
                            successState.sighReleaseState is SighReleaseState.Idle &&
                            !isFlightInProgress,
                    onExplosionFinished = { origin ->
                        pendingFlightOrigin = origin
                        viewModel.registerSighAfterExplosion()
                    },
                    onMicrophoneError = { error ->
                        if (error == BreathInputError.PermissionDenied) {
                            showMicrophonePermissionDialog = true
                        } else {
                            microphoneError = error
                        }
                    },
                    ensureLocationPermission = {
                        when (viewModel.ensureLocationPermission()) {
                            LocationPermissionStatus.Granted -> {
                                true
                            }

                            LocationPermissionStatus.PermanentlyDenied -> {
                                showLocationPermissionDialog = true
                                false
                            }

                            LocationPermissionStatus.Denied -> {
                                false
                            }
                        }
                    },
                )
            },
            onZoomInClick = viewModel::onZoomInClick,
            onZoomOutClick = viewModel::onZoomOutClick,
            onMyLocationClick = viewModel::onMyLocationClick,
            errorMessage = uiState.toBannerMessage(),
            sighReleaseState = successState?.sighReleaseState ?: SighReleaseState.Idle,
            onRetrySigh = viewModel::retrySighRegistration,
            onCancelSigh = {
                pendingFlightOrigin = null
                viewModel.cancelFailedSighRegistration()
            },
            microphoneErrorMessage = microphoneError?.toKoreanMessage(),
            onMicrophoneErrorDismiss = { microphoneError = null },
        )

        val activeId = activeFlightId
        val origin = pendingFlightOrigin
        val destination = activeId?.let { projectionSnapshot.points[it] }
        if (activeId != null && origin != null && destination != null && successState?.focusRequest?.id == activeId) {
            StarFlightOverlay(
                flight = animationCoordinator.start(activeId, origin, Offset(destination.xPx, destination.yPx)),
                onLanded = { id ->
                    if (id !in highlightedFeatureIds) highlightedFeatureIds += id
                    pendingFlightOrigin = null
                    activeFlightId = null
                    landedFlightId = id
                    isFlightInProgress = false
                    viewModel.consumeFocusRequest(id)
                },
                onCancelled = { id ->
                    if (activeFlightId == id) {
                        pendingFlightOrigin = null
                        activeFlightId = null
                        isFlightInProgress = false
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        LandingHighlightOverlay(
            featureIds = highlightedFeatureIds,
            projectedPoints = projectionSnapshot.points,
            onExpired = { highlightedFeatureIds.remove(it) },
            modifier = Modifier.fillMaxSize(),
        )

        if (showLocationPermissionDialog) {
            AppDialog(
                title = "위치 권한 설정 안내",
                body = "한숨을 별로 만들려면 위치 권한이 필요합니다.\n설정에서 위치 권한을 '허용'으로 변경해주세요.",
                confirmText = "설정으로 이동",
                onConfirmClick = {
                    showLocationPermissionDialog = false
                    viewModel.openLocationSettings()
                },
                onDismissRequest = { showLocationPermissionDialog = false },
                onDismissClick = { showLocationPermissionDialog = false },
                dismissText = "취소",
            )
        }

        if (showMicrophonePermissionDialog) {
            AppDialog(
                title = "마이크 권한 설정 안내",
                body = "한숨을 불려면 마이크 권한이 필요합니다.\n설정에서 마이크 권한을 '허용'으로 변경해주세요.",
                confirmText = "설정으로 이동",
                onConfirmClick = {
                    showMicrophonePermissionDialog = false
                    viewModel.openAppSettings()
                },
                onDismissRequest = { showMicrophonePermissionDialog = false },
                onDismissClick = { showMicrophonePermissionDialog = false },
                dismissText = "취소",
            )
        }
    }
}

private fun MapUiState.toBannerMessage(): String? =
    when (this) {
        is MapUiState.Error -> {
            message
        }

        is MapUiState.Success -> {
            when (val release = sighReleaseState) {
                is SighReleaseState.Error -> release.message
                else -> refreshErrorMessage ?: (locationState as? LocationState.Unavailable)?.reason?.toKoreanMessage()
            }
        }

        MapUiState.Loading -> {
            null
        }
    }

private fun MapUiState.Success.toMapRenderState(hiddenMarkerId: String?): MapRenderState =
    MapRenderState(
        currentLocation = (locationState as? LocationState.Available)?.location,
        locationState = locationState,
        fallbackCenter = DEFAULT_MAP_POINT,
        sighMarkers =
            sighs.filterNot { it.id.toString() == hiddenMarkerId }.map { sighPin ->
                SighMarker(
                    id = sighPin.id.toString(),
                    latitude = sighPin.coordinate.latitude,
                    longitude = sighPin.coordinate.longitude,
                )
            },
        focusRequest = focusRequest,
    )

private val DEFAULT_MAP_POINT = MapPoint("default-location", 37.5505, 127.0373)

@Preview
@Composable
private fun MapScreenPreview() {
    AppTheme { MapScreen(locationDependencies = null, onSettingsClick = {}) }
}
