package com.classitda.feature.instructor.mypage.contract

import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.MemberListPage
import com.classitda.domain.model.instructor.mypage.MemberSortOrder

sealed interface MemberManagementUiState {
    data object Loading : MemberManagementUiState

    data class Content(
        val page: MemberListPage,
        val query: String = "",
        val sortOrder: MemberSortOrder = MemberSortOrder.RECENTLY_REGISTERED,
        val actionState: MemberManagementActionState = MemberManagementActionState.Hidden,
    ) : MemberManagementUiState

    data class Empty(
        val sortOrder: MemberSortOrder = MemberSortOrder.RECENTLY_REGISTERED,
    ) : MemberManagementUiState

    data class SearchEmpty(
        val query: String,
        val sortOrder: MemberSortOrder = MemberSortOrder.RECENTLY_REGISTERED,
    ) : MemberManagementUiState

    data class Error(
        val reason: MemberManagementUiError,
    ) : MemberManagementUiState
}

enum class MemberManagementUiError {
    NETWORK,
    UNKNOWN,
}

sealed interface MemberManagementAction {
    data object Back : MemberManagementAction

    data class QueryChanged(
        val query: String,
    ) : MemberManagementAction

    data class SortOrderChanged(
        val sortOrder: MemberSortOrder,
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
