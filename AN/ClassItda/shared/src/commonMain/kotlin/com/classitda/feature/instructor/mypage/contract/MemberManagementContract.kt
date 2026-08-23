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
    ) : MemberManagementUiState

    data object Empty : MemberManagementUiState

    data class SearchEmpty(
        val query: String,
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

    data class OpenMember(
        val memberId: InstructorMemberId,
    ) : MemberManagementAction

    data object OpenMemberRegistration : MemberManagementAction

    data object Retry : MemberManagementAction
}
