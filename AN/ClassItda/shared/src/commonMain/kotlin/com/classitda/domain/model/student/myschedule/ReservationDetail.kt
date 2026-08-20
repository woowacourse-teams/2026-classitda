package com.classitda.domain.model.student.myschedule

import kotlin.time.Instant

sealed interface ReservationDetail {
    val reservationId: ReservationId
    val session: ClassSession

    data class Confirmed(
        override val reservationId: ReservationId,
        override val session: ClassSession,
        val reservedAt: Instant,
        val pass: MemberPassAvailability,
        val cancellation: ReservationCancellationAvailability,
    ) : ReservationDetail

    data class Cancelled(
        override val reservationId: ReservationId,
        override val session: ClassSession,
        val cancelledAt: Instant,
    ) : ReservationDetail

    data class ClassCancelled(
        override val reservationId: ReservationId,
        override val session: ClassSession,
        val cancelledAt: Instant,
    ) : ReservationDetail

    data class Attended(
        override val reservationId: ReservationId,
        override val session: ClassSession,
        val checkedInAt: Instant,
        val usedPass: MemberPassSummary,
    ) : ReservationDetail

    data class Absent(
        override val reservationId: ReservationId,
        override val session: ClassSession,
        val usedPass: MemberPassSummary,
    ) : ReservationDetail
}
