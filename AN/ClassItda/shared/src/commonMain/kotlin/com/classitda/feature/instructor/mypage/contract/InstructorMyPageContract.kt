package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.InstructorMyPageSummary

sealed interface InstructorMyPageUiState {
    data object Loading : InstructorMyPageUiState

    data class Content(
        val summary: InstructorMyPageSummary,
    ) : InstructorMyPageUiState

    data class Error(
        val reason: InstructorMyPageUiError,
    ) : InstructorMyPageUiState
}

enum class InstructorMyPageUiError {
    NETWORK,
    NOT_FOUND,
    UNKNOWN,
}

sealed interface InstructorMyPageAction {
    data object OpenProfile : InstructorMyPageAction

    data object OpenMemberManagement : InstructorMyPageAction

    data object OpenFacilityManagement : InstructorMyPageAction

    data object OpenPrivacyPolicy : InstructorMyPageAction

    data object Retry : InstructorMyPageAction
}
