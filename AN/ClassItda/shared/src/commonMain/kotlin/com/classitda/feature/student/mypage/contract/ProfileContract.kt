package com.classitda.feature.student.mypage.contract

import com.classitda.domain.model.student.mypage.MemberProfile
import com.classitda.domain.repository.student.mypage.MyPageFailureReason

sealed interface ProfileViewUiState {
    data object Loading : ProfileViewUiState

    data class Content(
        val profile: MemberProfile,
    ) : ProfileViewUiState

    data class Error(
        val reason: MyPageFailureReason,
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
        val profile: MemberProfile,
        val draftName: String,
        val canSave: Boolean,
    ) : ProfileEditUiState

    data class Saving(
        val profile: MemberProfile,
        val draftName: String,
    ) : ProfileEditUiState

    data class SaveFailed(
        val profile: MemberProfile,
        val draftName: String,
        val reason: MyPageFailureReason,
    ) : ProfileEditUiState

    data class Error(
        val reason: MyPageFailureReason,
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
