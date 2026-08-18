package com.classitda.domain.model.student.myschedule

import kotlin.time.Instant

sealed interface UpcomingSchedule {
    val session: ClassSession

    data class ConfirmedReservation(
        val reservationId: ReservationId,
        override val session: ClassSession,
        val reservedAt: Instant,
    ) : UpcomingSchedule

    data class Waitlisted(
        val waitlistId: WaitlistId,
        override val session: ClassSession,
        val appliedAt: Instant,
        val currentPosition: Int,
    ) : UpcomingSchedule {
        init {
            require(currentPosition >= 1) { "현재 대기 순번은 1 이상이어야 합니다." }
        }
    }
}
