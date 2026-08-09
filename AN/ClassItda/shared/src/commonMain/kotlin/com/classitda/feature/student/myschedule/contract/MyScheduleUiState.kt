package com.classitda.feature.student.myschedule.contract

sealed interface MyScheduleUiState {
    data class ScheduleList(
        val selectedTab: MyScheduleTab,
        val content: MyScheduleContentState,
    ) : MyScheduleUiState

    data class ScheduleDetail(
        val item: ActiveScheduleItemUiModel,
        val cancellationFlow: MyScheduleCancellationFlowState = MyScheduleCancellationFlowState.Idle,
    ) : MyScheduleUiState

    data class CancellationResult(
        val result: MyScheduleCancellationResultUiModel,
    ) : MyScheduleUiState
}
