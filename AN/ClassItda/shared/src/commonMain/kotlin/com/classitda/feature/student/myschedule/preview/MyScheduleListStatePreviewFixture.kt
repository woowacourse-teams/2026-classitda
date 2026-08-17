package com.classitda.feature.student.myschedule.preview

import com.classitda.domain.model.student.myschedule.ReservationId
import com.classitda.feature.student.myschedule.contract.MyScheduleListErrorUiModel
import com.classitda.feature.student.myschedule.contract.MyScheduleRefreshState
import com.classitda.feature.student.myschedule.contract.MyScheduleTab
import com.classitda.feature.student.myschedule.contract.MyScheduleUiState
import com.classitda.feature.student.myschedule.contract.UpcomingDateSectionUiModel
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleCardUiModel
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleTabState
import com.classitda.feature.student.myschedule.contract.UsageHistoryTabState

internal object MyScheduleListStatePreviewFixture {
    val content =
        MyScheduleUiState(
            upcoming =
                UpcomingScheduleTabState.Content(
                    sections = MyScheduleUpcomingPreviewFixture.sections,
                ),
            usageHistory =
                UsageHistoryTabState.Content(
                    sections = MyScheduleUsageHistoryPreviewFixture.sections,
                ),
        )

    val upcomingLoading =
        content.copy(upcoming = UpcomingScheduleTabState.Loading)

    val upcomingEmpty =
        content.copy(upcoming = UpcomingScheduleTabState.Empty)

    val historyEmpty =
        content.copy(
            selectedTab = MyScheduleTab.HISTORY,
            usageHistory = UsageHistoryTabState.Empty,
        )

    val upcomingError =
        content.copy(
            upcoming =
                UpcomingScheduleTabState.Error(
                    error = MyScheduleListErrorUiModel.NETWORK,
                ),
        )

    val upcomingRefreshing =
        content.copy(
            upcoming =
                UpcomingScheduleTabState.Content(
                    sections = MyScheduleUpcomingPreviewFixture.sections,
                    refreshState = MyScheduleRefreshState.Refreshing,
                ),
        )

    val refreshFailed =
        content.copy(
            upcoming =
                UpcomingScheduleTabState.Content(
                    sections = MyScheduleUpcomingPreviewFixture.sections,
                    refreshState =
                        MyScheduleRefreshState.Failed(
                            error = MyScheduleListErrorUiModel.NETWORK,
                        ),
                ),
            usageHistory =
                UsageHistoryTabState.Content(
                    sections = MyScheduleUsageHistoryPreviewFixture.sections,
                    refreshState =
                        MyScheduleRefreshState.Failed(
                            error = MyScheduleListErrorUiModel.NETWORK,
                        ),
                ),
        )

    val asyncInteraction =
        refreshFailed.copy(
            upcoming =
                UpcomingScheduleTabState.Error(
                    error = MyScheduleListErrorUiModel.NETWORK,
                ),
        )

    val longContent =
        content.copy(
            upcoming =
                UpcomingScheduleTabState.Content(
                    sections =
                        listOf(
                            UpcomingDateSectionUiModel(
                                dateLabel = "8월 8일 토요일",
                                items =
                                    listOf(
                                        UpcomingScheduleCardUiModel.ConfirmedReservation(
                                            reservationId = ReservationId("preview-long-content"),
                                            timeRangeLabel = "오후 7:30 ~ 8:20",
                                            title =
                                                "초보자부터 숙련자까지 함께하는 아주 긴 이름의 " +
                                                    "리포머 코어 밸런스 수업",
                                            instructorName =
                                                "아주 긴 이름을 가진 이지은 시니어 필라테스 강사",
                                            memo =
                                                "오늘은 건물 안쪽의 하타룸으로 오신 뒤 안내 데스크에 " +
                                                    "예약자 성함을 말씀해 주세요.",
                                        ),
                                    ),
                            ),
                        ),
                ),
        )
}
