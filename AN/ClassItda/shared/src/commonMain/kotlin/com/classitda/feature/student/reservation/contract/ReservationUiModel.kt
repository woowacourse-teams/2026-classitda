package com.classitda.feature.student.reservation.contract

internal data class ReservationClassUiModel(
    val id: String,
    val classTime: String,
    val className: String,
    val instructorName: String,
    val memo: String?,
    val leftStudentCount: Int,
    val cardType: ReservationClassCardType,
)

internal data class ReservationPassUiModel(
    val id: String,
    val name: String,
    val remainingText: String,
    val validityPeriodText: String,
)

internal enum class ReservationClassCardType {
    DEFAULT,
    RESERVED,
    WAITLISTED,
}
