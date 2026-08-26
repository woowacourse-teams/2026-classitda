package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.InstructorStudioId

sealed interface StudioManagementUiState {
    data object Loading : StudioManagementUiState

    data class Content(
        val page: StudioListUiModel,
        val successNotice: StudioSuccessNotice = StudioSuccessNotice.Hidden,
    ) : StudioManagementUiState

    data object Empty : StudioManagementUiState

    data class Error(
        val reason: StudioManagementUiError,
    ) : StudioManagementUiState
}

enum class StudioSuccessNotice {
    Hidden,
    Visible,
}

enum class StudioManagementUiError {
    NETWORK,
    UNKNOWN,
}

sealed interface StudioManagementAction {
    data object Back : StudioManagementAction

    data class EditStudio(
        val studioId: InstructorStudioId,
    ) : StudioManagementAction

    data class OpenStudioDetail(
        val studioId: InstructorStudioId,
    ) : StudioManagementAction

    data object OpenStudioRegistration : StudioManagementAction

    data object Retry : StudioManagementAction
}
