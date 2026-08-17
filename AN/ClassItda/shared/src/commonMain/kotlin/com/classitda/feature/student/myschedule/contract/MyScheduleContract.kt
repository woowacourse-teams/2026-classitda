package com.classitda.feature.student.myschedule.contract

data class MyScheduleUiState(
    val selectedTab: MyScheduleTab = MyScheduleTab.UPCOMING,
    val upcoming: UpcomingScheduleTabState = UpcomingScheduleTabState.NotLoaded,
    val usageHistory: UsageHistoryTabState = UsageHistoryTabState.NotLoaded,
)

sealed interface UpcomingScheduleTabState {
    data object NotLoaded : UpcomingScheduleTabState

    data object Loading : UpcomingScheduleTabState

    data object Empty : UpcomingScheduleTabState

    data class Content(
        val sections: List<UpcomingDateSectionUiModel>,
        val refreshState: MyScheduleRefreshState = MyScheduleRefreshState.Idle,
    ) : UpcomingScheduleTabState {
        init {
            require(sections.isNotEmpty()) {
                "예정 일정이 없을 때는 Content가 아니라 Empty 상태를 사용해야 합니다."
            }
        }
    }

    data class Error(
        val error: MyScheduleListErrorUiModel,
    ) : UpcomingScheduleTabState
}

sealed interface UsageHistoryTabState {
    data object NotLoaded : UsageHistoryTabState

    data object Loading : UsageHistoryTabState

    data object Empty : UsageHistoryTabState

    data class Content(
        val sections: List<UsageHistoryMonthSectionUiModel>,
        val refreshState: MyScheduleRefreshState = MyScheduleRefreshState.Idle,
    ) : UsageHistoryTabState {
        init {
            require(sections.isNotEmpty()) {
                "이용 내역이 없을 때는 Content가 아니라 Empty 상태를 사용해야 합니다."
            }
        }
    }

    data class Error(
        val error: MyScheduleListErrorUiModel,
    ) : UsageHistoryTabState
}

sealed interface MyScheduleRefreshState {
    data object Idle : MyScheduleRefreshState

    data object Refreshing : MyScheduleRefreshState

    data class Failed(
        val error: MyScheduleListErrorUiModel,
    ) : MyScheduleRefreshState
}

enum class MyScheduleListErrorUiModel {
    NETWORK,
    UNKNOWN,
}

sealed interface MyScheduleAction {
    data class SelectTab(
        val tab: MyScheduleTab,
    ) : MyScheduleAction

    data class Retry(
        val tab: MyScheduleTab,
    ) : MyScheduleAction
}
