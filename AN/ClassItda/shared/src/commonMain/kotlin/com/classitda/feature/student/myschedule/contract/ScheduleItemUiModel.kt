package com.classitda.feature.student.myschedule.contract

data class ScheduleItemId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "일정 ID는 비어 있을 수 없습니다." }
    }
}

sealed interface MyScheduleItemUiModel {
    val id: ScheduleItemId
    val title: String
    val locationLabel: String
    val instructorName: String
}

sealed interface UpcomingScheduleItemUiModel : MyScheduleItemUiModel {
    val dateTime: UpcomingScheduleDateTimeUiModel
    val cancellation: ScheduleCancellationAvailabilityUiModel

    data class ConfirmedReservation(
        override val id: ScheduleItemId,
        override val title: String,
        override val dateTime: UpcomingScheduleDateTimeUiModel,
        override val locationLabel: String,
        override val instructorName: String,
        val origin: ScheduleReservationOriginUiModel,
        override val cancellation: ScheduleCancellationAvailabilityUiModel,
    ) : UpcomingScheduleItemUiModel

    data class Waitlist(
        override val id: ScheduleItemId,
        override val title: String,
        override val dateTime: UpcomingScheduleDateTimeUiModel,
        override val locationLabel: String,
        override val instructorName: String,
        val position: Int,
        override val cancellation: ScheduleCancellationAvailabilityUiModel,
    ) : UpcomingScheduleItemUiModel {
        init {
            require(position > 0) { "대기 순번은 1 이상이어야 합니다." }
        }
    }
}

data class HistoryScheduleItemUiModel(
    override val id: ScheduleItemId,
    override val title: String,
    val dateTime: HistoryScheduleDateTimeUiModel,
    override val locationLabel: String,
    override val instructorName: String,
    val status: HistoryScheduleStatusUiModel,
) : MyScheduleItemUiModel

enum class ScheduleReservationOriginUiModel {
    DIRECT,
    CONFIRMED_FROM_WAITLIST,
}

enum class HistoryScheduleStatusUiModel {
    COMPLETED,
    RESERVATION_CANCELED,
}
