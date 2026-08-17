package com.classitda.feature.student.myschedule.contract

import com.classitda.domain.model.student.myschedule.ReservationId
import com.classitda.domain.model.student.myschedule.WaitlistId
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class MyScheduleListUiModelTest {
    @Test
    fun `예정 일정 날짜 구획은 예약과 대기의 서로 다른 식별자를 유지한다`() {
        val reservationId = ReservationId("reservation-1")
        val waitlistId = WaitlistId("waitlist-1")
        val section =
            UpcomingDateSectionUiModel(
                dateLabel = "8월 8일 토요일",
                items =
                    listOf(
                        createConfirmedReservation(reservationId),
                        createWaitlisted(waitlistId),
                    ),
            )

        assertEquals(
            reservationId,
            assertIs<UpcomingScheduleCardUiModel.ConfirmedReservation>(section.items[0]).reservationId,
        )
        assertEquals(
            waitlistId,
            assertIs<UpcomingScheduleCardUiModel.Waitlisted>(section.items[1]).waitlistId,
        )
    }

    @Test
    fun `이용 내역 상태는 출석 결석 예약 취소를 모두 구분한다`() {
        assertContentEquals(
            listOf(
                UsageHistoryStatusUiModel.ATTENDED,
                UsageHistoryStatusUiModel.ABSENT,
                UsageHistoryStatusUiModel.RESERVATION_CANCELLED,
            ),
            UsageHistoryStatusUiModel.entries,
        )
    }

    @Test
    fun `예정 일정 날짜 구획에 일정이 없으면 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            UpcomingDateSectionUiModel(
                dateLabel = "8월 8일 토요일",
                items = emptyList(),
            )
        }
    }

    @Test
    fun `이용 내역 월 구획에 내역이 없으면 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            UsageHistoryMonthSectionUiModel(
                monthLabel = "2026년 8월",
                items = emptyList(),
            )
        }
    }

    private fun createConfirmedReservation(
        reservationId: ReservationId,
    ): UpcomingScheduleCardUiModel.ConfirmedReservation =
        UpcomingScheduleCardUiModel.ConfirmedReservation(
            reservationId = reservationId,
            timeRangeLabel = "오후 7:30 ~ 8:20",
            title = "리포머 밸런스",
            instructorName = "이지은 강사",
            memo = "오늘은 하타룸으로 오세요~",
        )

    private fun createWaitlisted(waitlistId: WaitlistId): UpcomingScheduleCardUiModel.Waitlisted =
        UpcomingScheduleCardUiModel.Waitlisted(
            waitlistId = waitlistId,
            timeRangeLabel = "오전 11:00 ~ 오후 12:50",
            title = "엄청나게 긴 글자의 수업",
            instructorName = "이지은 강사",
            memo = null,
        )
}
