package com.classitda.feature.common.profile.contract

enum class ProfileUiError {
    NETWORK,
    NOT_FOUND,
    CONFLICT,
    INVALID_REQUEST,
    UNKNOWN,
}

sealed interface ProfileViewUiState {
    data object Loading : ProfileViewUiState

    data class Content(
        val profile: MemberProfileUiModel,
    ) : ProfileViewUiState

    data class Error(
        val reason: ProfileUiError,
    ) : ProfileViewUiState
}

sealed interface ProfileViewAction {
    data object Back : ProfileViewAction

    data object Retry : ProfileViewAction

    data object OpenEdit : ProfileViewAction

    data object RequestLogout : ProfileViewAction

    data object RequestWithdrawal : ProfileViewAction
}

sealed interface ProfileEditUiState {
    data object Loading : ProfileEditUiState

    data class Editing(
        val profile: MemberProfileUiModel,
        val phoneNumber: String,
        val draftName: String,
        val canSave: Boolean,
    ) : ProfileEditUiState

    data class Saving(
        val profile: MemberProfileUiModel,
        val phoneNumber: String,
        val draftName: String,
    ) : ProfileEditUiState

    data class SaveFailed(
        val profile: MemberProfileUiModel,
        val phoneNumber: String,
        val draftName: String,
        val reason: ProfileUiError,
    ) : ProfileEditUiState

    data class Error(
        val reason: ProfileUiError,
    ) : ProfileEditUiState
}

sealed interface ProfileEditAction {
    data object Back : ProfileEditAction

    data object Retry : ProfileEditAction

    data class NameChanged(
        val name: String,
    ) : ProfileEditAction

    data object RequestPhotoChange : ProfileEditAction

    data object OpenPhoneNumberChange : ProfileEditAction

    data object Save : ProfileEditAction
}
