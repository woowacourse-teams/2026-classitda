package com.classitda.domain.repository.student.myschedule

import com.classitda.domain.model.student.myschedule.ClassPeriod
import com.classitda.domain.model.student.myschedule.ClassSession
import com.classitda.domain.model.student.myschedule.ClassSessionId
import com.classitda.domain.model.student.myschedule.FacilityId
import com.classitda.domain.model.student.myschedule.FacilitySummary
import com.classitda.domain.model.student.myschedule.InstructorId
import com.classitda.domain.model.student.myschedule.InstructorSummary
import com.classitda.domain.model.student.myschedule.MemberPassAvailability
import com.classitda.domain.model.student.myschedule.MemberPassId
import com.classitda.domain.model.student.myschedule.MemberPassSummary
import com.classitda.domain.model.student.myschedule.PassRestoration
import com.classitda.domain.model.student.myschedule.ReservationCancellationAvailability
import com.classitda.domain.model.student.myschedule.ReservationCancellationReceipt
import com.classitda.domain.model.student.myschedule.ReservationDetail
import com.classitda.domain.model.student.myschedule.ReservationId
import com.classitda.domain.model.student.myschedule.UpcomingSchedule
import com.classitda.domain.model.student.myschedule.UsageHistoryEntry
import com.classitda.domain.model.student.myschedule.UsageHistoryStatus
import com.classitda.domain.model.student.myschedule.WaitlistCancellationAvailability
import com.classitda.domain.model.student.myschedule.WaitlistCancellationReceipt
import com.classitda.domain.model.student.myschedule.WaitlistDetail
import com.classitda.domain.model.student.myschedule.WaitlistId
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Instant

class FakeMyScheduleRepositoryTest {
    @Test
    fun `빈 목록으로 설정하면 두 목록 조회는 성공한 빈 목록을 반환한다`() =
        runBlocking {
            val repository = FakeMyScheduleRepository()

            val upcoming = assertIs<MyScheduleResult.Success<List<UpcomingSchedule>>>(repository.getUpcomingSchedules())
            val history = assertIs<MyScheduleResult.Success<List<UsageHistoryEntry>>>(repository.getUsageHistory())

            assertEquals(emptyList(), upcoming.value)
            assertEquals(emptyList(), history.value)
        }

    @Test
    fun `조회 실패를 설정하면 요청 ID를 기록하고 설정한 실패를 반환한다`() =
        runBlocking {
            val reservationId = ReservationId("reservation-fetch-failure")
            val waitlistId = WaitlistId("waitlist-fetch-failure")
            val repository =
                FakeMyScheduleRepository(
                    failures =
                        FakeMyScheduleFailures(
                            upcomingSchedules = MyScheduleFailureReason.NETWORK,
                            usageHistory = MyScheduleFailureReason.CONFLICT,
                            reservationDetail = MyScheduleFailureReason.UNKNOWN,
                            waitlistDetail = MyScheduleFailureReason.NOT_FOUND,
                        ),
                )

            val upcoming = assertIs<MyScheduleResult.Failure>(repository.getUpcomingSchedules())
            val history = assertIs<MyScheduleResult.Failure>(repository.getUsageHistory())
            val reservationDetail =
                assertIs<MyScheduleResult.Failure>(repository.getReservationDetail(reservationId))
            val waitlistDetail =
                assertIs<MyScheduleResult.Failure>(repository.getWaitlistDetail(waitlistId))

            assertEquals(MyScheduleFailureReason.NETWORK, upcoming.reason)
            assertEquals(MyScheduleFailureReason.CONFLICT, history.reason)
            assertEquals(MyScheduleFailureReason.UNKNOWN, reservationDetail.reason)
            assertEquals(MyScheduleFailureReason.NOT_FOUND, waitlistDetail.reason)
            assertEquals(reservationId, repository.lastReservationDetailRequestId)
            assertEquals(waitlistId, repository.lastWaitlistDetailRequestId)
        }

    @Test
    fun `취소 실패를 설정하면 요청 ID만 기록하고 저장 상태는 변경하지 않는다`() =
        runBlocking {
            val fixture = createFixture()
            val repository =
                fixture.createRepository(
                    failures =
                        FakeMyScheduleFailures(
                            reservationCancellation = MyScheduleFailureReason.CONFLICT,
                            waitlistCancellation = MyScheduleFailureReason.NETWORK,
                        ),
                )
            val upcomingBefore = repository.upcomingSchedulesSnapshot
            val historyBefore = repository.usageHistorySnapshot

            val reservationResult = repository.cancelReservation(fixture.reservationId)
            val waitlistResult = repository.cancelWaitlist(fixture.waitlistId)

            assertEquals(
                MyScheduleResult.Failure(MyScheduleFailureReason.CONFLICT),
                reservationResult,
            )
            assertEquals(
                MyScheduleResult.Failure(MyScheduleFailureReason.NETWORK),
                waitlistResult,
            )
            assertEquals(fixture.reservationId, repository.lastReservationCancellationRequestId)
            assertEquals(fixture.waitlistId, repository.lastWaitlistCancellationRequestId)
            assertEquals(upcomingBefore, repository.upcomingSchedulesSnapshot)
            assertEquals(historyBefore, repository.usageHistorySnapshot)
            assertIs<MyScheduleResult.Success<ReservationDetail>>(
                repository.getReservationDetail(fixture.reservationId),
            )
            assertIs<MyScheduleResult.Success<WaitlistDetail>>(
                repository.getWaitlistDetail(fixture.waitlistId),
            )
            Unit
        }

    @Test
    fun `예약 취소 성공 시 같은 ID의 예정 일정을 취소 내역과 취소 상세로 바꾼다`() =
        runBlocking {
            val fixture = createFixture()
            val repository = fixture.createRepository()

            val result =
                assertIs<MyScheduleResult.Success<ReservationCancellationReceipt>>(
                    repository.cancelReservation(fixture.reservationId),
                )

            assertEquals(fixture.reservationId, repository.lastReservationCancellationRequestId)
            assertEquals(fixture.reservationId, result.value.reservationId)
            assertEquals(CANCELLED_AT, result.value.cancelledAt)
            assertEquals(RESTORATION, result.value.restoration)
            assertNull(
                repository.upcomingSchedulesSnapshot
                    .filterIsInstance<UpcomingSchedule.ConfirmedReservation>()
                    .find { it.reservationId == fixture.reservationId },
            )
            val cancelledHistory =
                repository.usageHistorySnapshot.single { it.reservationId == fixture.reservationId }
            assertEquals(UsageHistoryStatus.RESERVATION_CANCELLED, cancelledHistory.status)
            assertEquals(fixture.reservationSession, cancelledHistory.session)

            val detail =
                assertIs<MyScheduleResult.Success<ReservationDetail>>(
                    repository.getReservationDetail(fixture.reservationId),
                )
            val cancelledDetail = assertIs<ReservationDetail.Cancelled>(detail.value)
            assertEquals(fixture.reservationId, repository.lastReservationDetailRequestId)
            assertEquals(fixture.reservationId, cancelledDetail.reservationId)
            assertEquals(CANCELLED_AT, cancelledDetail.cancelledAt)
        }

    @Test
    fun `대기 취소 성공 시 같은 ID의 예정 대기만 제거하고 이용 내역은 추가하지 않는다`() =
        runBlocking {
            val fixture = createFixture()
            val repository = fixture.createRepository()
            val historyBefore = repository.usageHistorySnapshot

            val result =
                assertIs<MyScheduleResult.Success<WaitlistCancellationReceipt>>(
                    repository.cancelWaitlist(fixture.waitlistId),
                )

            assertEquals(fixture.waitlistId, repository.lastWaitlistCancellationRequestId)
            assertEquals(fixture.waitlistId, result.value.waitlistId)
            assertEquals(2, result.value.positionAtCancellation)
            assertNull(
                repository.upcomingSchedulesSnapshot
                    .filterIsInstance<UpcomingSchedule.Waitlisted>()
                    .find { it.waitlistId == fixture.waitlistId },
            )
            assertEquals(historyBefore, repository.usageHistorySnapshot)
            assertEquals(
                MyScheduleResult.Failure(MyScheduleFailureReason.NOT_FOUND),
                repository.getWaitlistDetail(fixture.waitlistId),
            )
            assertEquals(fixture.waitlistId, repository.lastWaitlistDetailRequestId)
        }

    private fun createFixture(): RepositoryFixture {
        val reservationId = ReservationId("reservation-1")
        val waitlistId = WaitlistId("waitlist-1")
        val reservationSession = createSession("reservation-session")
        val waitlistSession = createSession("waitlist-session")
        val upcomingReservation =
            UpcomingSchedule.ConfirmedReservation(
                reservationId = reservationId,
                session = reservationSession,
                reservedAt = Instant.parse("2026-08-01T06:20:00Z"),
            )
        val upcomingWaitlist =
            UpcomingSchedule.Waitlisted(
                waitlistId = waitlistId,
                session = waitlistSession,
                appliedAt = Instant.parse("2026-08-03T12:20:00Z"),
                currentPosition = 2,
            )
        val reservationDetail =
            ReservationDetail.Confirmed(
                reservationId = reservationId,
                session = reservationSession,
                reservedAt = upcomingReservation.reservedAt,
                pass = createPassAvailability(),
                cancellation = ReservationCancellationAvailability.Available(restoredPassUses = 1),
            )
        val waitlistDetail =
            WaitlistDetail(
                waitlistId = waitlistId,
                session = waitlistSession,
                appliedAt = upcomingWaitlist.appliedAt,
                currentPosition = upcomingWaitlist.currentPosition,
                pass = createPassAvailability(),
                cancellation = WaitlistCancellationAvailability.Available,
            )

        return RepositoryFixture(
            reservationId = reservationId,
            waitlistId = waitlistId,
            reservationSession = reservationSession,
            upcomingSchedules = listOf(upcomingReservation, upcomingWaitlist),
            reservationDetail = reservationDetail,
            waitlistDetail = waitlistDetail,
        )
    }

    private fun createSession(id: String): ClassSession =
        ClassSession(
            id = ClassSessionId(id),
            title = "체어 밸런스",
            period =
                ClassPeriod(
                    startsAt = Instant.parse("2026-08-04T09:30:00Z"),
                    endsAt = Instant.parse("2026-08-04T10:20:00Z"),
                    timeZoneId = "Asia/Seoul",
                ),
            instructor =
                InstructorSummary(
                    id = InstructorId("instructor-$id"),
                    name = "이지은 강사",
                    profileImageUrl = null,
                ),
            facility =
                FacilitySummary(
                    id = FacilityId("facility-$id"),
                    name = "하타룸",
                ),
            memo = "수업 메모",
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
            remainingUses = 4,
            reservableUses = 3,
            cancellableUses = 2,
        )

    private data class RepositoryFixture(
        val reservationId: ReservationId,
        val waitlistId: WaitlistId,
        val reservationSession: ClassSession,
        val upcomingSchedules: List<UpcomingSchedule>,
        val reservationDetail: ReservationDetail,
        val waitlistDetail: WaitlistDetail,
    ) {
        fun createRepository(failures: FakeMyScheduleFailures = FakeMyScheduleFailures()): FakeMyScheduleRepository =
            FakeMyScheduleRepository(
                upcomingSchedules = upcomingSchedules,
                usageHistory =
                    listOf(
                        UsageHistoryEntry(
                            reservationId = ReservationId("existing-history"),
                            session = reservationSession,
                            status = UsageHistoryStatus.ATTENDED,
                        ),
                    ),
                reservationDetails = listOf(reservationDetail),
                waitlistDetails = listOf(waitlistDetail),
                failures = failures,
                cancelledAt = CANCELLED_AT,
                reservationRestoration = RESTORATION,
            )
    }

    private companion object {
        val CANCELLED_AT: Instant = Instant.parse("2026-08-18T01:30:00Z")
        val RESTORATION =
            PassRestoration(
                restoredUses = 1,
                remainingUsesAfterCancellation = 5,
            )
    }
}
