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
import com.pheeeew.domain.model.geo.Coordinate
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel { MapViewModel(FakeSighRepository()) },
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSighReleaseDialogVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val sighReleaseState = (uiState as? MapTempUiState.Success)?.sighReleaseState

    LaunchedEffect(viewModel) {
        viewModel.sighReleasedEvents.collect {
            isSighReleaseDialogVisible = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        MapOverlay(
            onSettingsClick = onSettingsClick,
            onRefreshClick = viewModel::loadSighs,
            onSighLongPress = {
                if (uiState is MapTempUiState.Success) {
                    isSighReleaseDialogVisible = true
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("연결 상태를 확인해주세요!")
                    }
                }
            },
            onZoomInClick = {},
            onZoomOutClick = {},
            onMyLocationClick = {},
            errorMessage = (uiState as? MapTempUiState.Error)?.message,
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        if (isSighReleaseDialogVisible) {
            SighReleaseDialog(
                sighReleaseState = sighReleaseState ?: SighReleaseState.Idle,
                onSendClick = {
                    // TODO: LocationRepository가 없어 임시 좌표를 사용한다. 실제 GPS 좌표로 교체 필요.
                    viewModel.sendSigh(Coordinate(latitude = 0.0, longitude = 0.0))
                },
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

@Preview
@Composable
private fun MapScreenPreview() {
    AppTheme {
        MapScreen(onSettingsClick = {})
    }
}
