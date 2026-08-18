package com.classitda.feature.student.mypage.contract

import com.classitda.domain.model.student.mypage.ConnectedFacility
import com.classitda.domain.repository.student.mypage.MyPageFailureReason

sealed interface ConnectedFacilitiesUiState {
    data object Loading : ConnectedFacilitiesUiState

    data object Empty : ConnectedFacilitiesUiState

    data class Content(
        val facilities: List<ConnectedFacility>,
    ) : ConnectedFacilitiesUiState

    data class Error(
        val reason: MyPageFailureReason,
    ) : ConnectedFacilitiesUiState
}

sealed interface ConnectedFacilitiesAction {
    data object Back : ConnectedFacilitiesAction

    data object Retry : ConnectedFacilitiesAction
}
