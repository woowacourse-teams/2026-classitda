package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.ManagedMember

sealed interface MemberDetailUiState {
    data object Loading : MemberDetailUiState

    data class Content(
        val member: ManagedMember,
        val deleteState: MemberDeleteState = MemberDeleteState.Hidden,
    ) : MemberDetailUiState

    data class Error(
        val reason: MemberDetailUiError,
    ) : MemberDetailUiState

    data class Deleted(
        val memberId: InstructorMemberId,
    ) : MemberDetailUiState
}

sealed interface MemberDeleteState {
    data object Hidden : MemberDeleteState

    data class Confirming(
        val typedName: String = "",
        val error: MemberDeleteError? = null,
    ) : MemberDeleteState

    data object Submitting : MemberDeleteState

    data class Failed(
        val typedName: String,
        val reason: MemberDeleteError,
    ) : MemberDeleteState
}

enum class MemberDetailUiError {
    NETWORK,
    NOT_FOUND,
    UNKNOWN,
}

enum class MemberDeleteError {
    NAME_MISMATCH,
    NETWORK,
    NOT_FOUND,
    UNKNOWN,
}

sealed interface MemberDetailAction {
    data object Back : MemberDetailAction

    data object OpenEdit : MemberDetailAction

    data object RequestDelete : MemberDetailAction

    data class DeleteNameChanged(
        val name: String,
    ) : MemberDetailAction

    data object CancelDelete : MemberDetailAction

    data object ConfirmDelete : MemberDetailAction

    data object Retry : MemberDetailAction
}
