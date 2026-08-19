package com.classitda.feature.student.myschedule.contract

import com.classitda.domain.model.student.myschedule.ReservationId
import com.classitda.domain.model.student.myschedule.WaitlistId

sealed interface UpcomingScheduleCardUiModel {
    val timeRangeLabel: String
    val title: String
    val instructorName: String

    data class ConfirmedReservation(
        val reservationId: ReservationId,
        override val timeRangeLabel: String,
        override val title: String,
        override val instructorName: String,
    ) : UpcomingScheduleCardUiModel

    data class Waitlisted(
        val waitlistId: WaitlistId,
        val currentPosition: Int,
        override val timeRangeLabel: String,
        override val title: String,
        override val instructorName: String,
    ) : UpcomingScheduleCardUiModel {
        init {
            require(currentPosition >= 0) { "대기 순번은 0 이상이어야 합니다." }
        }
    }
}

data class UpcomingDateSectionUiModel(
    val dateLabel: String,
    val items: List<UpcomingScheduleCardUiModel>,
) {
    init {
        require(dateLabel.isNotBlank()) { "예정 일정 날짜 표시는 비어 있을 수 없습니다." }
        require(items.isNotEmpty()) { "예정 일정 날짜 구획에는 일정이 하나 이상 있어야 합니다." }
    }
}

data class UsageHistoryCardUiModel(
    val reservationId: ReservationId,
    val dateTimeLabel: String,
    val title: String,
    val instructorName: String,
    val status: UsageHistoryStatusUiModel,
)

enum class UsageHistoryStatusUiModel {
    ATTENDED,
    ABSENT,
    CLASS_CANCELLED,
    RESERVATION_CANCELLED,
}

data class UsageHistoryMonthSectionUiModel(
    val monthLabel: String,
    val items: List<UsageHistoryCardUiModel>,
) {
    init {
        require(monthLabel.isNotBlank()) { "이용 내역 월 표시는 비어 있을 수 없습니다." }
        require(items.isNotEmpty()) { "이용 내역 월 구획에는 내역이 하나 이상 있어야 합니다." }
    }
}
