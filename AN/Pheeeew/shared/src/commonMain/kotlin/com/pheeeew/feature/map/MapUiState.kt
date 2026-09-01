package com.pheeeew.feature.map

import com.pheeeew.domain.model.location.LocationState
import com.pheeeew.domain.model.sigh.SighPin
import com.pheeeew.feature.map.map.MapCameraCommand

sealed interface MapUiState {
    data object Loading : MapUiState // 초기 로딩, 보여줄 콘텐츠 없음

    data class Error(
        val message: String,
    ) : MapUiState // 초기 로딩 실패, 보여줄 콘텐츠 없음

    data class Success(
        val sighs: List<SighPin>,
        val locationState: LocationState,
        val cameraCommand: MapCameraCommand?,
        val isRequestingLocation: Boolean,
        val sighReleaseState: SighReleaseState = SighReleaseState.Idle,
    ) : MapUiState
}

sealed interface SighReleaseState {
    data object Idle : SighReleaseState

    data object Submitting : SighReleaseState

    data class Error(
        val message: String,
    ) : SighReleaseState
}
