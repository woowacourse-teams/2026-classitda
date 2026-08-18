package com.classitda.domain.model.student.myschedule

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.time.Instant

class ScheduleListTest {
    @Test
    fun `예약 일정은 ReservationId와 수업 정보를 유지한다`() {
        val reservationId = ReservationId("reservation-1")
        val session = createSession()
        val reservedAt = Instant.parse("2026-08-01T01:00:00Z")

        val schedule: UpcomingSchedule =
            UpcomingSchedule.ConfirmedReservation(
                reservationId = reservationId,
                session = session,
                reservedAt = reservedAt,
            )

        val confirmedReservation = assertIs<UpcomingSchedule.ConfirmedReservation>(schedule)
        assertEquals(reservationId, confirmedReservation.reservationId)
        assertEquals(session, confirmedReservation.session)
        assertEquals(reservedAt, confirmedReservation.reservedAt)
    }

    @Test
    fun `대기 일정은 WaitlistId와 수업 정보를 유지한다`() {
        val waitlistId = WaitlistId("waitlist-1")
        val session = createSession()
        val appliedAt = Instant.parse("2026-08-02T01:00:00Z")

        val schedule: UpcomingSchedule =
            UpcomingSchedule.Waitlisted(
                waitlistId = waitlistId,
                session = session,
                appliedAt = appliedAt,
                currentPosition = 2,
            )

        val waitlisted = assertIs<UpcomingSchedule.Waitlisted>(schedule)
        assertEquals(waitlistId, waitlisted.waitlistId)
        assertEquals(session, waitlisted.session)
        assertEquals(appliedAt, waitlisted.appliedAt)
    }

    @Test
    fun `현재 대기 순번이 1이면 생성할 수 있다`() {
        val schedule = createWaitlisted(currentPosition = 1)

        assertEquals(1, schedule.currentPosition)
    }

    @Test
    fun `현재 대기 순번이 0이면 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            createWaitlisted(currentPosition = 0)
        }
    }

    @Test
    fun `현재 대기 순번이 음수이면 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            createWaitlisted(currentPosition = -1)
        }
    }

    @Test
    fun `이용 내역의 모든 상태는 ReservationId와 수업 정보를 유지한다`() {
        val statuses =
            listOf(
                UsageHistoryStatus.ATTENDED,
                UsageHistoryStatus.ABSENT,
                UsageHistoryStatus.RESERVATION_CANCELLED,
            )
        val session = createSession()
        val history =
            statuses.mapIndexed { index, status ->
                UsageHistoryEntry(
                    reservationId = ReservationId("reservation-${index + 1}"),
                    session = session,
                    status = status,
                )
            }

        assertContentEquals(UsageHistoryStatus.entries, history.map(UsageHistoryEntry::status))
        assertContentEquals(
            listOf(
                ReservationId("reservation-1"),
                ReservationId("reservation-2"),
                ReservationId("reservation-3"),
            ),
            history.map(UsageHistoryEntry::reservationId),
        )
        history.forEach { entry -> assertEquals(session, entry.session) }
    }

    private fun createWaitlisted(currentPosition: Int): UpcomingSchedule.Waitlisted =
        UpcomingSchedule.Waitlisted(
            waitlistId = WaitlistId("waitlist-1"),
            session = createSession(),
            appliedAt = Instant.parse("2026-08-02T01:00:00Z"),
            currentPosition = currentPosition,
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
