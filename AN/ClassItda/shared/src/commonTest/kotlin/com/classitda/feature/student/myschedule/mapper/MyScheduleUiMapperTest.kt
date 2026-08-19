package com.classitda.feature.student.myschedule.mapper

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
import com.classitda.feature.student.myschedule.contract.ReservationCancellationAvailabilityUiModel
import com.classitda.feature.student.myschedule.contract.ReservationDetailUiModel
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleCardUiModel
import com.classitda.feature.student.myschedule.contract.UsageHistoryStatusUiModel
import com.classitda.feature.student.myschedule.contract.WaitlistCancellationAvailabilityUiModel
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.time.Instant

class MyScheduleUiMapperTest {
    private val mapper =
        MyScheduleUiMapper(
            locale = MyScheduleDisplayLocale.KOREAN,
            currentTimeProvider = CurrentTimeProvider { FIXED_NOW },
        )

    @Test
    fun `예정 일정은 시작 시각 오름차순으로 날짜 구획과 시간 표시를 만든다`() {
        val schedules =
            listOf(
                createWaitlisted(
                    id = "waitlist-august-9",
                    session =
                        createSession(
                            id = "session-august-9",
                            startsAt = "2026-08-09T02:00:00Z",
                            endsAt = "2026-08-09T03:50:00Z",
                        ),
                ),
                createConfirmed(
                    id = "reservation-august-8-evening",
                    session =
                        createSession(
                            id = "session-august-8-evening",
                            startsAt = "2026-08-08T10:30:00Z",
                            endsAt = "2026-08-08T11:20:00Z",
                        ),
                ),
                createConfirmed(
                    id = "reservation-august-8-morning",
                    session =
                        createSession(
                            id = "session-august-8-morning",
                            startsAt = "2026-08-08T02:00:00Z",
                            endsAt = "2026-08-08T03:50:00Z",
                        ),
                ),
            )

        val sections = mapper.mapUpcomingSchedules(schedules)

        assertContentEquals(listOf("8월 8일 토요일", "8월 9일 일요일"), sections.map { it.dateLabel })
        assertContentEquals(
            listOf("오전 11:00 ~ 오후 12:50", "오후 7:30 ~ 8:20"),
            sections.first().items.map { it.timeRangeLabel },
        )
        assertEquals(
            ReservationId("reservation-august-8-morning"),
            assertIs<UpcomingScheduleCardUiModel.ConfirmedReservation>(sections.first().items[0]).reservationId,
        )
        assertEquals(
            WaitlistId("waitlist-august-9"),
            assertIs<UpcomingScheduleCardUiModel.Waitlisted>(sections.last().items.single()).waitlistId,
        )
        assertEquals(
            2,
            assertIs<UpcomingScheduleCardUiModel.Waitlisted>(sections.last().items.single()).currentPosition,
        )
    }

    @Test
    fun `이용 내역은 최근 순으로 월 구획을 만들고 날짜에서 요일과 상태를 계산한다`() {
        val history =
            listOf(
                createHistory(
                    id = "reservation-august-1",
                    status = UsageHistoryStatus.ABSENT,
                    session =
                        createSession(
                            id = "session-august-1",
                            startsAt = "2026-08-01T01:00:00Z",
                            endsAt = "2026-08-01T04:50:00Z",
                        ),
                ),
                createHistory(
                    id = "reservation-september-2",
                    status = UsageHistoryStatus.RESERVATION_CANCELLED,
                    session =
                        createSession(
                            id = "session-september-2",
                            startsAt = "2026-09-02T09:30:00Z",
                            endsAt = "2026-09-02T10:20:00Z",
                        ),
                ),
                createHistory(
                    id = "reservation-august-4",
                    status = UsageHistoryStatus.ATTENDED,
                    session = createAugustFourthSession(),
                ),
            )

        val sections = mapper.mapUsageHistory(history)

        assertContentEquals(listOf("2026년 9월", "2026년 8월"), sections.map { it.monthLabel })
        assertContentEquals(
            listOf(
                "2026.08.04 (화) 오후 6:30 ~ 7:20",
                "2026.08.01 (토) 오전 10:00 ~ 오후 1:50",
            ),
            sections.last().items.map { it.dateTimeLabel },
        )
        assertContentEquals(
            listOf(UsageHistoryStatusUiModel.ATTENDED, UsageHistoryStatusUiModel.ABSENT),
            sections.last().items.map { it.status },
        )
        assertEquals(
            UsageHistoryStatusUiModel.RESERVATION_CANCELLED,
            sections
                .first()
                .items
                .single()
                .status,
        )

        val classCancelled =
            mapper.mapUsageHistory(
                listOf(
                    createHistory(
                        id = "reservation-class-cancelled",
                        status = UsageHistoryStatus.CLASS_CANCELLED,
                        session = createAugustFourthSession(),
                    ),
                ),
            )
        assertEquals(
            UsageHistoryStatusUiModel.CLASS_CANCELLED,
            classCancelled.single().items.single().status,
        )
    }

    @Test
    fun `예약 네 상태는 chip에 대응하는 타입과 상태별 날짜 출석 표시를 만든다`() {
        val session = createAugustFourthSession()
        val pass = createMemberPassSummary()
        val confirmed =
            mapper.mapReservationDetail(
                detail =
                    ReservationDetail.Confirmed(
                        reservationId = ReservationId("reservation-confirmed"),
                        session = session,
                        reservedAt = Instant.parse("2026-08-01T06:20:00Z"),
                        pass = createMemberPassAvailability(),
                        cancellation = ReservationCancellationAvailability.Available(restoredPassUses = 1),
                    ),
                cancellationDeadlineHoursBeforeStart = 4,
            )
        val cancelled =
            mapper.mapReservationDetail(
                detail =
                    ReservationDetail.Cancelled(
                        reservationId = ReservationId("reservation-cancelled"),
                        session = session,
                        cancelledAt = Instant.parse("2026-08-01T06:25:00Z"),
                    ),
                cancellationDeadlineHoursBeforeStart = 4,
            )
        val attended =
            mapper.mapReservationDetail(
                detail =
                    ReservationDetail.Attended(
                        reservationId = ReservationId("reservation-attended"),
                        session = session,
                        checkedInAt = Instant.parse("2026-08-04T09:20:00Z"),
                        usedPass = pass,
                    ),
                cancellationDeadlineHoursBeforeStart = 4,
            )
        val absent =
            mapper.mapReservationDetail(
                detail =
                    ReservationDetail.Absent(
                        reservationId = ReservationId("reservation-absent"),
                        session = session,
                        usedPass = pass,
                    ),
                cancellationDeadlineHoursBeforeStart = 4,
            )
        val classCancelled =
            mapper.mapReservationDetail(
                detail =
                    ReservationDetail.ClassCancelled(
                        reservationId = ReservationId("reservation-class-cancelled"),
                        session = session,
                        cancelledAt = Instant.parse("2026-08-01T06:25:00Z"),
                    ),
                cancellationDeadlineHoursBeforeStart = 4,
            )

        assertIs<ReservationDetailUiModel.Confirmed>(confirmed)
        assertIs<ReservationDetailUiModel.Cancelled>(cancelled)
        assertIs<ReservationDetailUiModel.ClassCancelled>(classCancelled)
        assertIs<ReservationDetailUiModel.Attended>(attended)
        assertIs<ReservationDetailUiModel.Absent>(absent)
        assertEquals("2026.08.04 (화)", confirmed.classInfo.dateLabel)
        assertEquals("2026년 8월 4일 화요일", attended.classInfo.dateLabel)
        assertEquals("2026.08.04 (화) 오후 6:20", attended.checkedInAtLabel)
        assertEquals("2026.08.04 (화)", absent.classInfo.dateLabel)
        assertEquals("--:--:--", absent.attendanceTimePlaceholder)
        assertFalse(confirmed.classInfo.timeRangeLabel.contains("50분"))
        assertFalse(attended.classInfo.timeRangeLabel.contains("50분"))
        assertFalse(absent.classInfo.timeRangeLabel.contains("50분"))
    }

    @Test
    fun `고정 현재 시각을 사용하면 수업 시작까지 남은 시간과 수강권 표시를 결정적으로 계산한다`() {
        val detail =
            ReservationDetail.Confirmed(
                reservationId = ReservationId("reservation-confirmed"),
                session = createAugustFourthSession(),
                reservedAt = Instant.parse("2026-08-01T06:20:00Z"),
                pass = createMemberPassAvailability(),
                cancellation = ReservationCancellationAvailability.Available(restoredPassUses = 1),
            )

        val uiModel =
            assertIs<ReservationDetailUiModel.Confirmed>(
                mapper.mapReservationDetail(
                    detail = detail,
                    cancellationDeadlineHoursBeforeStart = 4,
                ),
            )

        val cancellation =
            assertIs<ReservationCancellationAvailabilityUiModel.Available>(uiModel.cancellation)
        assertEquals(22, cancellation.hoursUntilStart)
        assertEquals(1, cancellation.restoredPassUses)
        assertEquals("2026.06.30 ~ 2026.08.20", uiModel.pass.validityLabel)
        assertEquals(2, uiModel.pass.cancellableUses)
    }

    @Test
    fun `대기 상세는 고정 시간대 표시와 대기 상태를 유지한다`() {
        val detail =
            WaitlistDetail(
                waitlistId = WaitlistId("waitlist-2"),
                session = createAugustFourthSession(),
                appliedAt = Instant.parse("2026-08-03T12:20:00Z"),
                currentPosition = 2,
                pass = createMemberPassAvailability(),
                cancellation = WaitlistCancellationAvailability.Available,
            )

        val uiModel = mapper.mapWaitlistDetail(detail)

        assertEquals("2026.08.03 (월) 오후 9:20", uiModel.appliedAtLabel)
        assertEquals("2026.08.04 (화)", uiModel.classInfo.dateLabel)
        assertEquals("오후 6:30 ~ 7:20", uiModel.classInfo.timeRangeLabel)
        assertEquals(2, uiModel.currentPosition)
        assertIs<WaitlistCancellationAvailabilityUiModel.Available>(uiModel.cancellation)
    }

    @Test
    fun `취소 영수증 mapper는 요청 ID와 결과 화면 표시 정보를 유지한다`() {
        val session = createAugustFourthSession()
        val reservationReceipt =
            ReservationCancellationReceipt(
                reservationId = ReservationId("reservation-receipt"),
                session = session,
                cancelledAt = Instant.parse("2026-08-04T05:32:00Z"),
                restoration =
                    PassRestoration(
                        restoredUses = 1,
                        remainingUsesAfterCancellation = 15,
                    ),
            )
        val waitlistReceipt =
            WaitlistCancellationReceipt(
                waitlistId = WaitlistId("waitlist-receipt"),
                session = session,
                cancelledAt = Instant.parse("2026-08-04T05:32:00Z"),
                positionAtCancellation = 2,
            )

        val reservationResult = mapper.mapReservationCancellationReceipt(reservationReceipt)
        val waitlistResult = mapper.mapWaitlistCancellationReceipt(waitlistReceipt)

        assertEquals(ReservationId("reservation-receipt"), reservationResult.reservationId)
        assertEquals("2026.08.04 (화) 오후 2:32", reservationResult.cancelledAtLabel)
        assertEquals(1, reservationResult.restoredPassUses)
        assertEquals(WaitlistId("waitlist-receipt"), waitlistResult.waitlistId)
        assertEquals("2026.08.04 (화)", waitlistResult.dateLabel)
        assertEquals("오후 6:30 ~ 7:20", waitlistResult.timeRangeLabel)
        assertEquals(2, waitlistResult.positionAtCancellation)
    }

    private fun createConfirmed(
        id: String,
        session: ClassSession,
    ): UpcomingSchedule.ConfirmedReservation =
        UpcomingSchedule.ConfirmedReservation(
            reservationId = ReservationId(id),
            session = session,
            reservedAt = Instant.parse("2026-08-01T06:20:00Z"),
        )

    private fun createWaitlisted(
        id: String,
        session: ClassSession,
        currentPosition: Int = 2,
    ): UpcomingSchedule.Waitlisted =
        UpcomingSchedule.Waitlisted(
            waitlistId = WaitlistId(id),
            session = session,
            appliedAt = Instant.parse("2026-08-03T12:20:00Z"),
            currentPosition = currentPosition,
        )

    private fun createHistory(
        id: String,
        status: UsageHistoryStatus,
        session: ClassSession,
    ): UsageHistoryEntry =
        UsageHistoryEntry(
            reservationId = ReservationId(id),
            session = session,
            status = status,
        )

    private fun createAugustFourthSession(): ClassSession =
        createSession(
            id = "session-august-4",
            startsAt = "2026-08-04T09:30:00Z",
            endsAt = "2026-08-04T10:20:00Z",
        )

    private fun createSession(
        id: String,
        startsAt: String,
        endsAt: String,
    ): ClassSession =
        ClassSession(
            id = ClassSessionId(id),
            title = "체어 밸런스",
            period =
                ClassPeriod(
                    startsAt = Instant.parse(startsAt),
                    endsAt = Instant.parse(endsAt),
                    timeZoneId = FIXED_TIME_ZONE_ID,
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
                    name = "클래스잇다 금토동지점",
                ),
            memo = "오늘 평소보다 난이도가 조금 있는 수업입니다.",
        )

    private fun createMemberPassSummary(): MemberPassSummary =
        MemberPassSummary(
            id = MemberPassId("member-pass-1"),
            name = "[8:1] 그룹 레슨 20회권",
            validFrom = LocalDate(2026, 6, 30),
            validUntil = LocalDate(2026, 8, 20),
        )

    private fun createMemberPassAvailability(): MemberPassAvailability =
        MemberPassAvailability(
            pass = createMemberPassSummary(),
            remainingUses = 14,
            reservableUses = 5,
            cancellableUses = 2,
        )

    private companion object {
        val FIXED_NOW: Instant = Instant.parse("2026-08-03T11:30:00Z")
        const val FIXED_TIME_ZONE_ID = "Asia/Seoul"
    }
}
