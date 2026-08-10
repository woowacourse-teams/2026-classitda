package com.classitda.feature.student.myschedule.contract

sealed interface MyScheduleUiState {
    data class ScheduleList(
        val selectedTab: MyScheduleTab,
        val upcomingContent: MyScheduleContentState<UpcomingScheduleItemUiModel>,
        val historyContent: MyScheduleContentState<HistoryScheduleItemUiModel>,
    ) : MyScheduleUiState

    data class ScheduleDetail(
        val item: UpcomingScheduleItemUiModel,
        val cancellationFlow: MyScheduleCancellationFlowState = MyScheduleCancellationFlowState.Idle,
    ) : MyScheduleUiState

    data class CancellationResult(
        val result: MyScheduleCancellationResultUiModel,
    ) : MyScheduleUiState
}
