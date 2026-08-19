package com.classitda.feature.student.mypage.contract

import com.classitda.domain.model.student.mypage.MyPageSummary
import com.classitda.domain.repository.student.mypage.MyPageFailureReason

sealed interface MyPageContentState {
    data object Loading : MyPageContentState

    data class Content(
        val summary: MyPageSummary,
        val uiModel: MyPageSummaryUiModel? = null,
    ) : MyPageContentState

    data class Error(
        val reason: MyPageFailureReason,
    ) : MyPageContentState
}

data class MyPageUiState(
    val content: MyPageContentState = MyPageContentState.Loading,
)

sealed interface MyPageAction {
    data object Retry : MyPageAction

    data object OpenProfile : MyPageAction

    data object OpenPasses : MyPageAction

    data object OpenConnectedFacilities : MyPageAction

    data object OpenNotificationSettings : MyPageAction

    data object OpenPrivacyPolicy : MyPageAction

    data object OpenInstructorSignup : MyPageAction

    data object SwitchToInstructor : MyPageAction
}
