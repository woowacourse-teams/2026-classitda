package com.classitda.data.repository.home

import com.classitda.domain.model.home.PendingReservation
import com.classitda.domain.model.home.ReservationPassSummary
import com.classitda.domain.model.home.UpcomingReservation
import com.classitda.domain.repository.home.ReservationRepository
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

// TODO: 실제 API 연동 시 remote 기반 구현으로 교체
class FakeReservationRepository : ReservationRepository {
    private val timeZone = TimeZone.currentSystemDefault()

    override suspend fun getPendingReservation(): PendingReservation {
        delay(300)
        val now = Clock.System.now()
        val startAt = (now + 18.minutes).toLocalDateTime(timeZone)
        val endAt = (now + 68.minutes).toLocalDateTime(timeZone)
        return PendingReservation(
            id = "pending-reservation-1",
            className = "리포머 밸런스",
            instructorName = "이지은",
            startAt = startAt,
            endAt = endAt,
            memo = "준비물 - 수건, 오늘 수업 조금 강도가 있어요.",
            pass =
                ReservationPassSummary(
                    passName = "리포머 20회권",
                    totalRemainingCount = 8,
                    reservableCount = 5,
                    cancellableCount = 2,
                ),
        )
    }

    override suspend fun getNextUpcomingReservation(): UpcomingReservation {
        delay(300)
        val now = Clock.System.now()
        val startAt = (now + 22.hours).toLocalDateTime(timeZone)
        val endAt = (now + 22.hours + 50.minutes).toLocalDateTime(timeZone)
        return UpcomingReservation(
            id = "upcoming-reservation-1",
            className = "리포머 밸런스",
            instructorName = "이지은",
            startAt = startAt,
            endAt = endAt,
            memo = "실내화를 지참해 주세요.",
        )
    }

    override suspend fun approveReservation(reservationId: String) {
        delay(300)
    }
}
