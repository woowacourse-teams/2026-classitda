package com.pheeeew.feature.map

import com.pheeeew.domain.model.sigh.SighPin

sealed interface MapTempUiState {
    data object Loading : MapTempUiState // 초기 로딩, 보여줄 콘텐츠 없음

    data class Error(
        val message: String,
    ) : MapTempUiState // 초기 로딩 실패, 보여줄 콘텐츠 없음

    data class Success(
        val sighs: List<SighPin>,
        val sighReleaseState: SighReleaseState = SighReleaseState.Idle,
    ) : MapTempUiState
}

sealed interface SighReleaseState {
    data object Idle : SighReleaseState

    data object Submitting : SighReleaseState

    data class Error(
        val message: String,
    ) : SighReleaseState
}
