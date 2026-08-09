package com.classitda.feature.student.myschedule.contract

data class ScheduleItemId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "일정 ID는 비어 있을 수 없습니다." }
    }
}

data class ScheduleDateTimeUiModel(
    val dateLabel: String,
    val timeLabel: String,
)

sealed interface ScheduleItemUiModel {
    val id: ScheduleItemId
    val title: String
    val dateTime: ScheduleDateTimeUiModel
    val locationLabel: String
    val instructorName: String

    data class ConfirmedReservation(
        override val id: ScheduleItemId,
        override val title: String,
        override val dateTime: ScheduleDateTimeUiModel,
        override val locationLabel: String,
        override val instructorName: String,
        val origin: ScheduleReservationOriginUiModel,
        val cancellation: ScheduleCancellationAvailabilityUiModel,
    ) : ActiveScheduleItemUiModel

    data class Waitlist(
        override val id: ScheduleItemId,
        override val title: String,
        override val dateTime: ScheduleDateTimeUiModel,
        override val locationLabel: String,
        override val instructorName: String,
        val position: Int,
        val cancellation: ScheduleCancellationAvailabilityUiModel,
    ) : ActiveScheduleItemUiModel {
        init {
            require(position > 0) { "대기 순번은 1 이상이어야 합니다." }
        }
    }

    data class History(
        override val id: ScheduleItemId,
        override val title: String,
        override val dateTime: ScheduleDateTimeUiModel,
        override val locationLabel: String,
        override val instructorName: String,
        val outcome: ScheduleHistoryOutcomeUiModel,
    ) : ScheduleItemUiModel
}

sealed interface ActiveScheduleItemUiModel : ScheduleItemUiModel

enum class ScheduleReservationOriginUiModel {
    DIRECT,
    CONFIRMED_FROM_WAITLIST,
}

enum class ScheduleHistoryOutcomeUiModel {
    COMPLETED,
    RESERVATION_CANCELED,
    NO_SHOW,
    EXPIRED,
}
