package com.classitda.feature.student.myschedule.contract

import com.classitda.domain.model.student.myschedule.ReservationId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class MyScheduleContractTest {
    @Test
    fun `기본 상태는 예정 일정 탭을 선택하고 두 탭을 아직 불러오지 않은 상태로 둔다`() {
        val state = MyScheduleUiState()

        assertEquals(MyScheduleTab.UPCOMING, state.selectedTab)
        assertIs<UpcomingScheduleTabState.NotLoaded>(state.upcoming)
        assertIs<UsageHistoryTabState.NotLoaded>(state.usageHistory)
    }

    @Test
    fun `탭 선택과 두 탭의 콘텐츠 상태는 서로 독립적으로 유지된다`() {
        val state =
            MyScheduleUiState(
                selectedTab = MyScheduleTab.HISTORY,
                upcoming =
                    UpcomingScheduleTabState.Error(
                        error = MyScheduleListErrorUiModel.NETWORK,
                    ),
                usageHistory = UsageHistoryTabState.Loading,
            )

        assertEquals(MyScheduleTab.HISTORY, state.selectedTab)
        assertIs<UpcomingScheduleTabState.Error>(state.upcoming)
        assertIs<UsageHistoryTabState.Loading>(state.usageHistory)
    }

    @Test
    fun `예정 일정 Content는 기본적으로 갱신 중이 아닌 상태다`() {
        val content =
            UpcomingScheduleTabState.Content(
                sections = listOf(createUpcomingSection()),
            )

        assertIs<MyScheduleRefreshState.Idle>(content.refreshState)
    }

    @Test
    fun `예정 일정 Content는 기존 내용을 유지한 채 갱신 중을 표현한다`() {
        val sections = listOf(createUpcomingSection())

        val content =
            UpcomingScheduleTabState.Content(
                sections = sections,
                refreshState = MyScheduleRefreshState.Refreshing,
            )

        assertEquals(sections, content.sections)
        assertIs<MyScheduleRefreshState.Refreshing>(content.refreshState)
    }

    @Test
    fun `이용 내역 Content는 기존 내용을 유지한 채 갱신 오류를 표현한다`() {
        val sections = listOf(createUsageHistorySection())
        val refreshError =
            MyScheduleRefreshState.Failed(
                error = MyScheduleListErrorUiModel.NETWORK,
            )

        val content =
            UsageHistoryTabState.Content(
                sections = sections,
                refreshState = refreshError,
            )

        assertEquals(sections, content.sections)
        assertEquals(refreshError, content.refreshState)
    }

    @Test
    fun `예정 일정이 비어 있으면 Content 상태를 만들 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            UpcomingScheduleTabState.Content(sections = emptyList())
        }
    }

    @Test
    fun `이용 내역이 비어 있으면 Content 상태를 만들 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            UsageHistoryTabState.Content(sections = emptyList())
        }
    }

    private fun createUpcomingSection(): UpcomingDateSectionUiModel =
        UpcomingDateSectionUiModel(
            dateLabel = "8월 8일 토요일",
            items =
                listOf(
                    UpcomingScheduleCardUiModel.ConfirmedReservation(
                        reservationId = ReservationId("reservation-1"),
                        timeRangeLabel = "오후 7:30 ~ 8:20",
                        title = "리포머 밸런스",
                        instructorName = "이지은 강사",
                        memo = "오늘은 하타룸으로 오세요~",
                    ),
                ),
        )

    private fun createUsageHistorySection(): UsageHistoryMonthSectionUiModel =
        UsageHistoryMonthSectionUiModel(
            monthLabel = "2026년 8월",
            items =
                listOf(
                    UsageHistoryCardUiModel(
                        reservationId = ReservationId("reservation-2"),
                        dateTimeLabel = "2026.08.04 (화) 오후 6:30 ~ 7:20",
                        title = "체어 밸런스",
                        instructorName = "이지은 강사",
                        memo = null,
                        status = UsageHistoryStatusUiModel.ATTENDED,
                    ),
                ),
        )
}
