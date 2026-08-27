package com.classitda.domain.model.student.myschedule

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ReservationDetailTest {
    @Test
    fun `예약 상세의 모든 상태는 동일한 ReservationId와 수업을 유지한다`() {
        val reservationId = ReservationId("reservation-1")
        val session = createSession()
        val passSummary = createPassSummary()
        val details =
            listOf(
                ReservationDetail.Confirmed(
                    reservationId = reservationId,
                    session = session,
                    reservedAt = Instant.parse("2026-08-01T01:00:00Z"),
                    pass = createPassAvailability(passSummary),
                    cancellation = ReservationCancellationAvailability.Available(restoredPassUses = 1),
                ),
                ReservationDetail.Cancelled(
                    reservationId = reservationId,
                    session = session,
                    cancelledAt = Instant.parse("2026-08-02T01:00:00Z"),
                ),
                ReservationDetail.ClassCancelled(
                    reservationId = reservationId,
                    session = session,
                    cancelledAt = Instant.parse("2026-08-03T01:00:00Z"),
                ),
                ReservationDetail.Attended(
                    reservationId = reservationId,
                    session = session,
                    checkedInAt = Instant.parse("2026-08-17T00:55:00Z"),
                    usedPass = passSummary,
                ),
                ReservationDetail.Absent(
                    reservationId = reservationId,
                    session = session,
                    usedPass = passSummary,
                ),
            )

        details.forEach { detail ->
            assertEquals(reservationId, detail.reservationId)
            assertEquals(session, detail.session)
        }
    }

    @Test
    fun `예약 완료 상세의 취소 가능 횟수는 수강권 가용 정보에서 가져온다`() {
        val detail =
            ReservationDetail.Confirmed(
                reservationId = ReservationId("reservation-1"),
                session = createSession(),
                reservedAt = Instant.parse("2026-08-01T01:00:00Z"),
                pass = createPassAvailability(cancellableUses = 2),
                cancellation = ReservationCancellationAvailability.Available(restoredPassUses = 1),
            )

        assertEquals(2, detail.pass.cancellableUses)
    }

    private fun createPassAvailability(
        pass: MemberPassSummary = createPassSummary(),
        cancellableUses: Int = 2,
    ): MemberPassAvailability =
        MemberPassAvailability(
            pass = pass,
            remainingUses = 5,
            reservableUses = 4,
            cancellableUses = cancellableUses,
        )

    private fun createPassSummary(): MemberPassSummary =
        MemberPassSummary(
            id = MemberPassId("member-pass-1"),
            name = "필라테스 10회권",
            validFrom = LocalDate(2026, 8, 1),
            validUntil = LocalDate(2026, 9, 30),
        )

    private fun createSession(): ClassSession =
        ClassSession(
            id = ClassSessionId("session-1"),
            title = "리포머 베이직",
            period =
                ClassPeriod(
                    startsAt = Instant.parse("2026-08-17T01:00:00Z"),
                    endsAt = Instant.parse("2026-08-17T02:00:00Z"),
                    timeZoneId = "Asia/Seoul",
                ),
            instructor =
                InstructorSummary(
                    id = InstructorId("instructor-1"),
                    name = "홍길동",
                    profileImageUrl = null,
                ),
            facility =
                FacilitySummary(
                    id = FacilityId("facility-1"),
                    name = "강남점",
                ),
            memo = null,
        )
}
