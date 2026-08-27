package com.classitda.domain.model.classreservation

internal data class ClassReservation(
    val id: String,
    val className: String,
    val dateText: String,
    val timeText: String,
    val instructorName: String,
    val memoText: String,
    val cancellationNotice: String,
    val classPasses: List<ClassPass>,
)

internal data class ClassPass(
    val id: String,
    val name: String,
    val usageText: String,
    val validityPeriodText: String,
)

internal sealed interface ReservationRequestResult {
    data object Success : ReservationRequestResult

    data class TimeConflict(
        val className: String,
        val dateTimeText: String,
        val studioName: String,
    ) : ReservationRequestResult

    data class Failure(
        val message: String,
    ) : ReservationRequestResult
}
