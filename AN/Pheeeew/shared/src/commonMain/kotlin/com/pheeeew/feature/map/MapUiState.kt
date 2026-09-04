package com.pheeeew.feature.map

import com.pheeeew.core.audio.BreathInputError
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
        val mapErrorMessage: String? = null,
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
        LocationError.PermissionDenied -> {
            "설정에서 위치 권한을 '허용'으로 변경해주세요."
        }

        LocationError.ServicesDisabled -> {
            "기기 설정에서 위치 서비스를 켜주세요."
        }

        LocationError.GpsUnavailable -> {
            "현재 위치를 확인할 수 없습니다."
        }

        LocationError.LocationTimeout -> {
            "현재 위치를 확인하는 데 시간이 걸리고 있습니다."
        }
    }

internal fun BreathInputError.toKoreanMessage(): String =
    when (this) {
        BreathInputError.PermissionDenied -> "마이크 권한이 필요해요"
        BreathInputError.MicrophoneUnavailable -> "마이크를 사용할 수 없어요"
        BreathInputError.StartFailed -> "마이크를 시작하지 못했어요. 다시 시도해주세요"
    }
