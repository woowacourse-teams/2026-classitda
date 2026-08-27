package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.InstructorMemberId

sealed interface MemberManagementUiState {
    data object Loading : MemberManagementUiState

    data object NoStudio : MemberManagementUiState

    data class Content(
        val page: MemberListUiModel,
        val query: String = "",
        val actionState: MemberManagementActionState = MemberManagementActionState.Hidden,
    ) : MemberManagementUiState

    data object Empty : MemberManagementUiState

    data class SearchEmpty(
        val query: String,
        val totalCount: Int = 0,
    ) : MemberManagementUiState

    data class Error(
        val reason: MemberManagementUiError,
    ) : MemberManagementUiState
}

enum class MemberManagementUiError {
    NETWORK,
    UNAUTHORIZED,
    FORBIDDEN,
    UNKNOWN,
}

sealed interface MemberManagementAction {
    data object Back : MemberManagementAction

    data class QueryChanged(
        val query: String,
    ) : MemberManagementAction

    data class EditMember(
        val memberId: InstructorMemberId,
    ) : MemberManagementAction

    data class RequestDelete(
        val memberId: InstructorMemberId,
    ) : MemberManagementAction

    data class DeleteNameChanged(
        val name: String,
    ) : MemberManagementAction

    data object CancelDelete : MemberManagementAction

    data object ConfirmDelete : MemberManagementAction

    data object DeleteAcknowledged : MemberManagementAction

    data object OpenMemberRegistration : MemberManagementAction

    data object OpenStudioRegistration : MemberManagementAction

    data object Retry : MemberManagementAction
}

sealed interface MemberManagementActionState {
    data object Hidden : MemberManagementActionState

    data class Confirming(
        val memberId: InstructorMemberId,
        val typedName: String = "",
        val error: MemberManagementDeleteError? = null,
    ) : MemberManagementActionState

    data class Submitting(
        val memberId: InstructorMemberId,
        val typedName: String,
    ) : MemberManagementActionState

    data class Failed(
        val memberId: InstructorMemberId,
        val typedName: String,
        val reason: MemberManagementDeleteError,
    ) : MemberManagementActionState

    data class Deleted(
        val memberId: InstructorMemberId,
    ) : MemberManagementActionState
}

enum class MemberManagementDeleteError {
    NAME_MISMATCH,
    NETWORK,
    NOT_FOUND,
    UNKNOWN,
}
