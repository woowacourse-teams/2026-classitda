package com.pheeeew.feature.map.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pheeeew.feature.map.MapUiState

@Composable
fun BreathMap(
    state: MapUiState,
    cameraCommand: MapCameraCommand?,
    onSighClick: (String) -> Unit,
    onMapError: (MapError) -> Unit,
    modifier: Modifier = Modifier,
) {
    NativeBreathMap(
        state = state,
        cameraCommand = cameraCommand,
        onSighClick = onSighClick,
        onMapError = onMapError,
        modifier = modifier,
    )
}
