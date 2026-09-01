package com.pheeeew.feature.map

import com.pheeeew.domain.model.location.LocationError
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
        val focusRequest: MapFocusRequest? = null,
        val refreshErrorMessage: String? = null,
    ) : MapUiState
}

sealed interface SighReleaseState {
    data object Idle : SighReleaseState

    data object Submitting : SighReleaseState

    data class Error(
        val message: String,
        val canRetry: Boolean = true,
    ) : SighReleaseState
}

internal fun LocationError.toKoreanMessage(): String =
    when (this) {
        LocationError.PermissionDenied -> "설정에서 위치 권한을 '허용'으로 변경해주세요."
        LocationError.GpsUnavailable, LocationError.LocationTimeout -> "GPS 수신이 원활하지 않습니다."
    }
