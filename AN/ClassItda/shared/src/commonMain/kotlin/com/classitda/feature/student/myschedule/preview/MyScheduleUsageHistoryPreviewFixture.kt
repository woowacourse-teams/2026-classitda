package com.classitda.feature.student.myschedule.preview

import com.classitda.domain.model.student.myschedule.ReservationId
import com.classitda.feature.student.myschedule.contract.MyScheduleTab
import com.classitda.feature.student.myschedule.contract.MyScheduleUiState
import com.classitda.feature.student.myschedule.contract.UsageHistoryCardUiModel
import com.classitda.feature.student.myschedule.contract.UsageHistoryMonthSectionUiModel
import com.classitda.feature.student.myschedule.contract.UsageHistoryStatusUiModel
import com.classitda.feature.student.myschedule.contract.UsageHistoryTabState
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

internal object MyScheduleUsageHistoryPreviewFixture {
    val attended =
        UsageHistoryCardUiModel(
            reservationId = ReservationId("preview-reservation-f02-attended"),
            dateTimeLabel =
                usageHistoryDateTimeLabel(
                    date = LocalDate(2026, 8, 4),
                    timeRangeLabel = "오후 6:30 ~ 7:20",
                ),
            title = "체어 밸런스",
            instructorName = "이지은 강사",
            status = UsageHistoryStatusUiModel.ATTENDED,
        )

    val absent =
        UsageHistoryCardUiModel(
            reservationId = ReservationId("preview-reservation-f02-absent"),
            dateTimeLabel =
                usageHistoryDateTimeLabel(
                    date = LocalDate(2026, 8, 1),
                    timeRangeLabel = "오전 10:00 ~ 오후 1:50",
                ),
            title = "리포머 베이직",
            instructorName = "박소연 대표 강사",
            status = UsageHistoryStatusUiModel.ABSENT,
        )

    val reservationCancelled =
        UsageHistoryCardUiModel(
            reservationId = ReservationId("preview-reservation-f02-cancelled"),
            dateTimeLabel =
                usageHistoryDateTimeLabel(
                    date = LocalDate(2026, 8, 2),
                    timeRangeLabel = "오후 2:00 ~ 2:50",
                ),
            title = "캐딜락 스트레칭",
            instructorName = "김민지 강사",
            status = UsageHistoryStatusUiModel.RESERVATION_CANCELLED,
        )

    val classCancelled =
        UsageHistoryCardUiModel(
            reservationId = ReservationId("preview-reservation-f02-class-cancelled"),
            dateTimeLabel =
                usageHistoryDateTimeLabel(
                    date = LocalDate(2026, 8, 3),
                    timeRangeLabel = "오후 2:00 ~ 2:50",
                ),
            title = "캐딜락 스트레칭",
            instructorName = "김민지 강사",
            status = UsageHistoryStatusUiModel.CLASS_CANCELLED,
        )

    val sections =
        listOf(
            UsageHistoryMonthSectionUiModel(
                monthLabel = "2026년 8월",
                items = listOf(attended, absent, classCancelled, reservationCancelled),
            ),
        )

    val state =
        MyScheduleUiState(
            selectedTab = MyScheduleTab.HISTORY,
            usageHistory = UsageHistoryTabState.Content(sections = sections),
        )
}

private fun usageHistoryDateTimeLabel(
    date: LocalDate,
    timeRangeLabel: String,
): String =
    buildString {
        append(date.year)
        append('.')
        append(
            date.month.number
                .toString()
                .padStart(length = 2, padChar = '0'),
        )
        append('.')
        append(
            date.day
                .toString()
                .padStart(length = 2, padChar = '0'),
        )
        append(" (")
        append(date.dayOfWeek.koreanShortLabel())
        append(") ")
        append(timeRangeLabel)
    }

private fun DayOfWeek.koreanShortLabel(): String =
    when (this) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }
