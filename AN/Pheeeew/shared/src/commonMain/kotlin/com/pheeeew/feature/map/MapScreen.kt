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
import com.pheeeew.core.designsystem.theme.AppTheme
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
