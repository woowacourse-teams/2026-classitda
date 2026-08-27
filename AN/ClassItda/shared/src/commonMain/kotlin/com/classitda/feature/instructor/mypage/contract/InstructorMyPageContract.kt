package com.classitda.feature.instructor.mypage.contract

sealed interface InstructorMyPageUiState {
    data object Loading : InstructorMyPageUiState

    data class Content(
        val profile: InstructorMyPageUiModel,
    ) : InstructorMyPageUiState

    data class Error(
        val reason: InstructorMyPageUiError,
    ) : InstructorMyPageUiState
}

data class InstructorMyPageUiModel(
    val name: String,
    val phoneNumberLabel: String,
    val profileImageUrl: String?,
    val avatarFallback: String,
)

enum class InstructorMyPageUiError {
    NETWORK,
    NOT_FOUND,
    UNKNOWN,
}

sealed interface InstructorMyPageAction {
    data object OpenProfile : InstructorMyPageAction

    data object OpenStudioManagement : InstructorMyPageAction

    data object OpenPrivacyPolicy : InstructorMyPageAction

    data object Retry : InstructorMyPageAction
}
