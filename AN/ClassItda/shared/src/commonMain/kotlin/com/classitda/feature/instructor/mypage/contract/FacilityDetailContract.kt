package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.ManagedFacility

sealed interface FacilityDetailUiState {
    data object Loading : FacilityDetailUiState

    data class Content(
        val facility: ManagedFacility,
        val deleteState: FacilityDeleteState = FacilityDeleteState.Hidden,
    ) : FacilityDetailUiState

    data class Error(
        val reason: FacilityDetailUiError,
    ) : FacilityDetailUiState

    data class Deleted(
        val facilityId: InstructorFacilityId,
    ) : FacilityDetailUiState
}

sealed interface FacilityDeleteState {
    data object Hidden : FacilityDeleteState

    data class Confirming(
        val typedName: String = "",
        val error: FacilityDeleteError? = null,
    ) : FacilityDeleteState

    data object Submitting : FacilityDeleteState

    data class Failed(
        val typedName: String,
        val reason: FacilityDeleteError,
    ) : FacilityDeleteState
}

enum class FacilityDetailUiError {
    NETWORK,
    NOT_FOUND,
    UNKNOWN,
}

enum class FacilityDeleteError {
    NAME_MISMATCH,
    NETWORK,
    NOT_FOUND,
    UNKNOWN,
}

sealed interface FacilityDetailAction {
    data object Back : FacilityDetailAction

    data object OpenEdit : FacilityDetailAction

    data object RequestDelete : FacilityDetailAction

    data class DeleteNameChanged(
        val name: String,
    ) : FacilityDetailAction

    data object CancelDelete : FacilityDetailAction

    data object ConfirmDelete : FacilityDetailAction

    data class DeleteAcknowledged(
        val facilityId: InstructorFacilityId,
    ) : FacilityDetailAction

    data object Retry : FacilityDetailAction
}
