package com.classitda.feature.student.myschedule.contract

sealed interface MyScheduleContentState {
    data object InitialLoading : MyScheduleContentState

    data object Empty : MyScheduleContentState

    data object InitialFailure : MyScheduleContentState

    data class Content(
        val items: List<ScheduleItemUiModel>,
        val refreshState: MyScheduleRefreshState = MyScheduleRefreshState.Idle,
    ) : MyScheduleContentState {
        init {
            require(items.isNotEmpty()) { "콘텐츠에는 일정이 하나 이상 있어야 합니다." }
        }
    }
}

sealed interface MyScheduleRefreshState {
    data object Idle : MyScheduleRefreshState

    data object Refreshing : MyScheduleRefreshState

    data object Failed : MyScheduleRefreshState
}
