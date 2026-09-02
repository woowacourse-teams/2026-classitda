package com.pheeeew.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pheeeew.core.audio.BreathInputError
import com.pheeeew.core.designsystem.component.AppDialog
import com.pheeeew.core.designsystem.theme.AppColors
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
import com.pheeeew.feature.map.overlay.ErrorSnackbar
import com.pheeeew.feature.map.overlay.MapOverlay
import com.pheeeew.feature.map.overlay.SighPhase
import com.pheeeew.feature.map.overlay.SwipeUpHint
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    locationDependencies: LocationDependencies?,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
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
    var showLocationServicesDialog by remember { mutableStateOf(false) }
    var showMicrophonePermissionDialog by remember { mutableStateOf(false) }
    var startupPermissionsChecked by remember { mutableStateOf(false) }
    val lifeCycleOwner = LocalLifecycleOwner.current
    var sighPhase by remember { mutableStateOf(SighPhase.Idle) }
    var cancelSignal by remember { mutableStateOf(0) }
    val isSighSubmitting = successState?.sighReleaseState is SighReleaseState.Submitting
    val isSighInteractionVisible = sighPhase != SighPhase.Idle || isSighSubmitting

    LaunchedEffect(lifeCycleOwner, isActive) {
        if (!isActive) startupPermissionsChecked = false
        lifeCycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (!isActive) return@repeatOnLifecycle
            if (!startupPermissionsChecked) {
                viewModel.ensureLocationPermission()
                startupPermissionsChecked = true
                launch { viewModel.refreshLocationPermission() }
            } else {
                viewModel.refreshLocationPermission()
            }
            awaitCancellation()
        }
    }

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
            controlsEnabled = sighPhase == SighPhase.Idle && !isSighSubmitting,
        )

        if (isSighInteractionVisible) {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.8f))
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { cancelSignal += 1 })
                            },
                )
                Text(
                    text =
                        if (isSighSubmitting) {
                            "별을 만드는 중이에요"
                        } else {
                            when (sighPhase) {
                                SighPhase.Listening -> "후– 하고\n한숨을 내쉬어보세요"
                                SighPhase.Quiet -> "한숨을 날려\n별을 만들어보세요"
                                SighPhase.NeedsMore -> "한숨을 더 크게 불어주세요"
                                SighPhase.Bursting -> ""
                                SighPhase.Idle -> ""
                            }
                        },
                    style = AppTheme.typography.screenTitle,
                    color = AppColors.Cream100,
                    modifier = Modifier.align(BiasAlignment(0f, -0.15f)),
                )

                if (sighPhase == SighPhase.Quiet) {
                    SwipeUpHint(
                        modifier =
                            Modifier
                                .align(BiasAlignment(0f, -0.15f))
                                .offset(y = 40.dp),
                    )
                }
            }
        }

        if (isActive) {
            Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding().align(Alignment.BottomCenter)) {
                if (!isSighSubmitting) {
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

                                LocationPermissionStatus.ServicesDisabled -> {
                                    showLocationServicesDialog = true
                                    false
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
                        onPhaseChanged = { sighPhase = it },
                        cancelSignal = cancelSignal,
                    )
                }
                ErrorSnackbar(
                    message = microphoneError?.toKoreanMessage(),
                    onDismiss = { microphoneError = null },
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, end = 16.dp),
                )
            }
        }

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

        if (showLocationServicesDialog) {
            AppDialog(
                title = "위치 서비스 설정 안내",
                body = "현재 위치를 확인하려면 기기 설정에서 위치 서비스를 켜주세요.",
                confirmText = "설정으로 이동",
                onConfirmClick = {
                    showLocationServicesDialog = false
                    viewModel.openLocationSettings()
                },
                onDismissRequest = { showLocationServicesDialog = false },
                onDismissClick = { showLocationServicesDialog = false },
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
