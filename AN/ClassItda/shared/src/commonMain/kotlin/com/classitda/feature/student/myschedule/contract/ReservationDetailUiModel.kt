package com.classitda.feature.student.myschedule.contract

data class ReservationDetailUiModel(
    val id: ScheduleItemId,
    val title: String,
    val instructorName: String,
    val dateTime: ReservationDetailDateTimeUiModel,
    val locationLabel: String,
    val ticket: ReservationTicketUiModel,
    val attendeeCount: Int,
    val cancellation: ScheduleCancellationAvailabilityUiModel,
) {
    init {
        require(attendeeCount > 0) { "예약 인원은 1명 이상이어야 합니다." }
        require(cancellation.policy is ScheduleCancellationPolicyUiModel.Reservation) {
            "예약 상세에는 예약 취소 정책이 필요합니다."
        }
    }
}

data class ReservationDetailDateTimeUiModel(
    val dateLabel: String,
    val timeRangeLabel: String,
)

data class ReservationTicketUiModel(
    val name: String,
    val validUntilLabel: String,
    val remainingReservationCount: Int,
) {
    init {
        require(remainingReservationCount >= 0) { "예약 가능 횟수는 0 이상이어야 합니다." }
    }
}
