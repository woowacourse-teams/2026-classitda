package com.pheeeew.feature.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pheeeew.core.designsystem.theme.AppTheme
import com.pheeeew.data.repository.FakeSighRepository
import com.pheeeew.di.LocationDependencies
import com.pheeeew.domain.model.location.LocationState
import com.pheeeew.feature.map.map.BreathMap
import com.pheeeew.feature.map.overlay.MapOverlay
import com.pheeeew.feature.map.overlay.SighReleaseDialog
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    locationDependencies: LocationDependencies?,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel { MapViewModel(FakeSighRepository(), locationDependencies) },
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSighReleaseDialogVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val successState = uiState as? MapUiState.Success

    LaunchedEffect(viewModel) {
        viewModel.sighReleasedEvents.collect {
            isSighReleaseDialogVisible = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (successState != null) {
            BreathMap(
                state = successState.toMapRenderState(),
                cameraCommand = successState.cameraCommand,
                onSighClick = {},
                onMapError = {},
                modifier = Modifier.fillMaxSize(),
            )
        }

        MapOverlay(
            onSettingsClick = onSettingsClick,
            onRefreshClick = viewModel::loadSighs,
            onSighLongPress = {
                if (successState != null) {
                    isSighReleaseDialogVisible = true
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("연결 상태를 확인해주세요!")
                    }
                }
            },
            onZoomInClick = viewModel::onZoomInClick,
            onZoomOutClick = viewModel::onZoomOutClick,
            onMyLocationClick = viewModel::onMyLocationClick,
            errorMessage = uiState.toBannerMessage(),
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        if (isSighReleaseDialogVisible) {
            SighReleaseDialog(
                sighReleaseState = successState?.sighReleaseState ?: SighReleaseState.Idle,
                onSendClick = viewModel::sendSigh,
                onCancelClick = {
                    viewModel.cancelSighRelease()
                    isSighReleaseDialogVisible = false
                },
                onDismissRequest = {
                    viewModel.cancelSighRelease()
                    isSighReleaseDialogVisible = false
                },
            )
        }
    }
}

private fun MapUiState.toBannerMessage(): String? =
    when (this) {
        is MapUiState.Error -> message
        is MapUiState.Success -> (locationState as? LocationState.Unavailable)?.reason?.toKoreanMessage()
        MapUiState.Loading -> null
    }

private fun MapUiState.Success.toMapRenderState(): MapRenderState =
    MapRenderState(
        currentLocation = (locationState as? LocationState.Available)?.location,
        locationState = locationState,
        fallbackCenter = DEFAULT_MAP_POINT,
        sighMarkers =
            sighs.map { sighPin ->
                SighMarker(
                    id = sighPin.id.toString(),
                    latitude = sighPin.coordinate.latitude,
                    longitude = sighPin.coordinate.longitude,
                )
            },
        focusRequest = null,
    )

private val DEFAULT_MAP_POINT =
    MapPoint(
        id = "default-location",
        latitude = 37.5505,
        longitude = 127.0373,
    )

@Preview
@Composable
private fun MapScreenPreview() {
    AppTheme {
        MapScreen(locationDependencies = null, onSettingsClick = {})
    }
}
