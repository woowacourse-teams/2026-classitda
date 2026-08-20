package com.classitda.domain.model.student.myschedule

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class CancellationTest {
    @Test
    fun `예약 취소 시 복구될 수강권 횟수가 0이면 생성할 수 있다`() {
        ReservationCancellationAvailability.Available(restoredPassUses = 0)
    }

    @Test
    fun `예약 취소 시 복구될 수강권 횟수가 음수이면 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            ReservationCancellationAvailability.Available(restoredPassUses = -1)
        }
    }

    @Test
    fun `복구 횟수와 취소 후 잔여 횟수가 0이면 생성할 수 있다`() {
        PassRestoration(
            restoredUses = 0,
            remainingUsesAfterCancellation = 0,
        )
    }

    @Test
    fun `복구 횟수가 음수이면 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            PassRestoration(
                restoredUses = -1,
                remainingUsesAfterCancellation = 0,
            )
        }
    }

    @Test
    fun `취소 후 잔여 횟수가 음수이면 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            PassRestoration(
                restoredUses = 0,
                remainingUsesAfterCancellation = -1,
            )
        }
    }

    @Test
    fun `현재 대기 순번이 0과 1이면 대기 상세를 생성할 수 있다`() {
        assertEquals(0, createWaitlistDetail(currentPosition = 0).currentPosition)
        val detail = createWaitlistDetail(currentPosition = 1)

        assertEquals(1, detail.currentPosition)
    }

    @Test
    fun `현재 대기 순번이 음수이면 대기 상세를 생성할 수 없다`() {
        assertFailsWith<IllegalArgumentException> {
            createWaitlistDetail(currentPosition = -1)
        }
    }

    @Test
    fun `취소 당시 대기 순번이 1이면 대기 취소 영수증을 생성할 수 있다`() {
        val receipt = createWaitlistCancellationReceipt(positionAtCancellation = 1)

        assertEquals(1, receipt.positionAtCancellation)
    }

    @Test
    fun `취소 당시 대기 순번이 0 또는 음수이면 대기 취소 영수증을 생성할 수 없다`() {
        listOf(0, -1).forEach { positionAtCancellation ->
            assertFailsWith<IllegalArgumentException> {
                createWaitlistCancellationReceipt(positionAtCancellation = positionAtCancellation)
            }
        }
    }

    @Test
    fun `예약 취소 영수증은 요청한 ReservationId를 유지한다`() {
        val requestedId = ReservationId("reservation-1")

        val receipt =
            ReservationCancellationReceipt(
                reservationId = requestedId,
                session = createSession(),
                cancelledAt = Instant.parse("2026-08-03T01:00:00Z"),
                restoration =
                    PassRestoration(
                        restoredUses = 1,
                        remainingUsesAfterCancellation = 4,
                    ),
            )

        assertEquals(requestedId, receipt.reservationId)
    }

    @Test
    fun `대기 취소 영수증은 요청한 WaitlistId를 유지한다`() {
        val requestedId = WaitlistId("waitlist-1")

        val receipt = createWaitlistCancellationReceipt(waitlistId = requestedId)

        assertEquals(requestedId, receipt.waitlistId)
    }

    private fun createWaitlistDetail(currentPosition: Int): WaitlistDetail =
        WaitlistDetail(
            waitlistId = WaitlistId("waitlist-1"),
            session = createSession(),
            appliedAt = Instant.parse("2026-08-02T01:00:00Z"),
            currentPosition = currentPosition,
            pass = createPassAvailability(),
            cancellation = WaitlistCancellationAvailability.Available,
        )

    private fun createWaitlistCancellationReceipt(
        waitlistId: WaitlistId = WaitlistId("waitlist-1"),
        positionAtCancellation: Int = 2,
    ): WaitlistCancellationReceipt =
        WaitlistCancellationReceipt(
            waitlistId = waitlistId,
            session = createSession(),
            cancelledAt = Instant.parse("2026-08-03T01:00:00Z"),
            positionAtCancellation = positionAtCancellation,
        )

    private fun createPassAvailability(): MemberPassAvailability =
        MemberPassAvailability(
            pass =
                MemberPassSummary(
                    id = MemberPassId("member-pass-1"),
                    name = "필라테스 10회권",
                    validFrom = LocalDate(2026, 8, 1),
                    validUntil = LocalDate(2026, 9, 30),
                ),
            remainingUses = 5,
            reservableUses = 4,
            cancellableUses = 2,
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
