package com.classitda.feature.student.myschedule.contract

sealed interface MyScheduleContentState<out T : MyScheduleItemUiModel> {
    data object InitialLoading : MyScheduleContentState<Nothing>

    data object Empty : MyScheduleContentState<Nothing>

    data object InitialFailure : MyScheduleContentState<Nothing>

    data class Content<T : MyScheduleItemUiModel>(
        val items: List<T>,
        val refreshState: MyScheduleRefreshState = MyScheduleRefreshState.Idle,
    ) : MyScheduleContentState<T> {
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
