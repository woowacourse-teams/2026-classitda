package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.InstructorStudioId

/** Studio deletion is unavailable until the backend exposes a delete endpoint. */
internal const val STUDIO_DELETE_ENABLED: Boolean = false

sealed interface StudioDetailUiState {
    data object Loading : StudioDetailUiState

    data class Content(
        val studio: StudioUiModel,
        val deleteState: StudioDeleteState = StudioDeleteState.Hidden,
    ) : StudioDetailUiState

    data class Error(
        val reason: StudioDetailUiError,
    ) : StudioDetailUiState

    data class Deleted(
        val studioId: InstructorStudioId,
    ) : StudioDetailUiState
}

sealed interface StudioDeleteState {
    data object Hidden : StudioDeleteState

    data class Confirming(
        val typedName: String = "",
        val error: StudioDeleteError? = null,
    ) : StudioDeleteState

    data object Submitting : StudioDeleteState

    data class Failed(
        val typedName: String,
        val reason: StudioDeleteError,
    ) : StudioDeleteState
}

enum class StudioDetailUiError {
    NETWORK,
    NOT_FOUND,
    UNKNOWN,
}

enum class StudioDeleteError {
    NAME_MISMATCH,
    NETWORK,
    NOT_FOUND,
    UNKNOWN,
}

sealed interface StudioDetailAction {
    data object Back : StudioDetailAction

    data object OpenEdit : StudioDetailAction

    data object RequestDelete : StudioDetailAction

    data class DeleteNameChanged(
        val name: String,
    ) : StudioDetailAction

    data object CancelDelete : StudioDetailAction

    data object ConfirmDelete : StudioDetailAction

    data class DeleteAcknowledged(
        val studioId: InstructorStudioId,
    ) : StudioDetailAction

    data object Retry : StudioDetailAction
}
