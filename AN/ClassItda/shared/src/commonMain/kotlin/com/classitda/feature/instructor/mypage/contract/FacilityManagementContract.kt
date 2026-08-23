package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.repository.instructor.mypage.FacilityList

sealed interface FacilityManagementUiState {
    data object Loading : FacilityManagementUiState

    data class Content(
        val page: FacilityList,
        val successNotice: FacilitySuccessNotice = FacilitySuccessNotice.Hidden,
    ) : FacilityManagementUiState

    data object Empty : FacilityManagementUiState

    data class Error(
        val reason: FacilityManagementUiError,
    ) : FacilityManagementUiState
}

enum class FacilitySuccessNotice {
    Hidden,
    Visible,
}

enum class FacilityManagementUiError {
    NETWORK,
    UNKNOWN,
}

sealed interface FacilityManagementAction {
    data object Back : FacilityManagementAction

    data class EditFacility(
        val facilityId: InstructorFacilityId,
    ) : FacilityManagementAction

    data class OpenFacilityDetail(
        val facilityId: InstructorFacilityId,
    ) : FacilityManagementAction

    data object OpenFacilityRegistration : FacilityManagementAction

    data object Retry : FacilityManagementAction
}
