package com.classitda.data.repository.student.myschedule

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
import com.classitda.domain.model.student.myschedule.ReservationDetail
import com.classitda.domain.model.student.myschedule.ReservationId
import com.classitda.domain.model.student.myschedule.UpcomingSchedule
import com.classitda.domain.model.student.myschedule.UsageHistoryEntry
import com.classitda.domain.model.student.myschedule.UsageHistoryStatus
import com.classitda.domain.model.student.myschedule.WaitlistCancellationAvailability
import com.classitda.domain.model.student.myschedule.WaitlistDetail
import com.classitda.domain.model.student.myschedule.WaitlistId
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

internal val MY_SCHEDULE_FAKE_CURRENT_TIME: Instant = Instant.parse("2026-08-18T00:00:00Z")

internal fun createDefaultMyScheduleFakeRepository(): FakeMyScheduleRepository {
    val pass =
        MemberPassSummary(
            id = MemberPassId("member-pass-pilates-10"),
            name = "필라테스 10회권",
            validFrom = LocalDate(2026, 7, 1),
            validUntil = LocalDate(2026, 9, 30),
        )
    val passAvailability =
        MemberPassAvailability(
            pass = pass,
            remainingUses = 4,
            reservableUses = 3,
            cancellableUses = 2,
        )

    val confirmedReservationId = ReservationId("reservation-upcoming-confirmed")
    val confirmedSession =
        createSession(
            id = "session-upcoming-confirmed",
            title = "체어 밸런스",
            startsAt = "2026-08-20T01:00:00Z",
            endsAt = "2026-08-20T02:00:00Z",
            instructorName = "이지은 강사",
            facilityName = "하타룸",
            memo = "개인 운동복과 미끄럼 방지 양말을 준비해 주세요.",
        )
    val confirmedReservedAt = Instant.parse("2026-08-10T03:30:00Z")

    val waitlistId = WaitlistId("waitlist-upcoming-pending")
    val waitlistSession =
        createSession(
            id = "session-upcoming-waitlist",
            title = "바렐 필라테스",
            startsAt = "2026-08-21T05:00:00Z",
            endsAt = "2026-08-21T06:00:00Z",
            instructorName = "김하늘 강사",
            facilityName = "바렐룸",
            memo = "수업 시작 10분 전까지 도착해 주세요.",
        )
    val waitlistAppliedAt = Instant.parse("2026-08-11T07:20:00Z")

    val approvalWaitlistId = WaitlistId("waitlist-upcoming-approval-required")
    val approvalWaitlistSession =
        createSession(
            id = "session-upcoming-approval-required",
            title = "바렐 필라테스",
            startsAt = "2026-08-21T03:00:00Z",
            endsAt = "2026-08-21T04:00:00Z",
            instructorName = "김하늘 강사",
            facilityName = "바렐룸",
            memo = "수업 시작 10분 전까지 도착해 주세요.",
        )
    val approvalWaitlistAppliedAt = Instant.parse("2026-08-10T07:20:00Z")

    val positionOneWaitlistId = WaitlistId("waitlist-upcoming-position-one")
    val positionOneWaitlistSession =
        createSession(
            id = "session-upcoming-position-one",
            title = "바렐 필라테스",
            startsAt = "2026-08-21T04:00:00Z",
            endsAt = "2026-08-21T05:00:00Z",
            instructorName = "김하늘 강사",
            facilityName = "바렐룸",
            memo = "수업 시작 10분 전까지 도착해 주세요.",
        )
    val positionOneWaitlistAppliedAt = Instant.parse("2026-08-11T06:20:00Z")

    val absentReservationId = ReservationId("reservation-history-absent")
    val absentSession =
        createSession(
            id = "session-history-absent",
            title = "리포머 베이직",
            startsAt = "2026-08-04T09:30:00Z",
            endsAt = "2026-08-04T10:20:00Z",
            instructorName = "박서연 강사",
            facilityName = "리포머룸",
            memo = null,
        )

    val attendedReservationId = ReservationId("reservation-history-attended")
    val attendedSession =
        createSession(
            id = "session-history-attended",
            title = "모닝 필라테스",
            startsAt = "2026-08-01T01:00:00Z",
            endsAt = "2026-08-01T01:50:00Z",
            instructorName = "최유진 강사",
            facilityName = "매트룸",
            memo = null,
        )

    val cancelledReservationId = ReservationId("reservation-history-cancelled")
    val cancelledSession =
        createSession(
            id = "session-history-cancelled",
            title = "매트 필라테스",
            startsAt = "2026-07-28T10:00:00Z",
            endsAt = "2026-07-28T10:50:00Z",
            instructorName = "정다은 강사",
            facilityName = "매트룸",
            memo = "편안한 복장으로 참여해 주세요.",
        )

    val classCancelledReservationId = ReservationId("reservation-history-class-cancelled")
    val classCancelledSession =
        createSession(
            id = "session-history-class-cancelled",
            title = "체어 밸런스",
            startsAt = "2026-07-26T01:00:00Z",
            endsAt = "2026-07-26T01:50:00Z",
            instructorName = "이지은 강사",
            facilityName = "하타룸",
            memo = null,
        )

    return FakeMyScheduleRepository(
        upcomingSchedules =
            listOf(
                UpcomingSchedule.ConfirmedReservation(
                    reservationId = confirmedReservationId,
                    session = confirmedSession,
                    reservedAt = confirmedReservedAt,
                ),
                UpcomingSchedule.Waitlisted(
                    waitlistId = waitlistId,
                    session = waitlistSession,
                    appliedAt = waitlistAppliedAt,
                    currentPosition = 2,
                ),
                UpcomingSchedule.Waitlisted(
                    waitlistId = approvalWaitlistId,
                    session = approvalWaitlistSession,
                    appliedAt = approvalWaitlistAppliedAt,
                    currentPosition = 0,
                ),
                UpcomingSchedule.Waitlisted(
                    waitlistId = positionOneWaitlistId,
                    session = positionOneWaitlistSession,
                    appliedAt = positionOneWaitlistAppliedAt,
                    currentPosition = 1,
                ),
            ),
        usageHistory =
            listOf(
                UsageHistoryEntry(
                    reservationId = absentReservationId,
                    session = absentSession,
                    status = UsageHistoryStatus.ABSENT,
                ),
                UsageHistoryEntry(
                    reservationId = attendedReservationId,
                    session = attendedSession,
                    status = UsageHistoryStatus.ATTENDED,
                ),
                UsageHistoryEntry(
                    reservationId = cancelledReservationId,
                    session = cancelledSession,
                    status = UsageHistoryStatus.RESERVATION_CANCELLED,
                ),
                UsageHistoryEntry(
                    reservationId = classCancelledReservationId,
                    session = classCancelledSession,
                    status = UsageHistoryStatus.CLASS_CANCELLED,
                ),
            ),
        reservationDetails =
            listOf(
                ReservationDetail.Confirmed(
                    reservationId = confirmedReservationId,
                    session = confirmedSession,
                    reservedAt = confirmedReservedAt,
                    pass = passAvailability,
                    cancellation = ReservationCancellationAvailability.Available(restoredPassUses = 1),
                ),
                ReservationDetail.Absent(
                    reservationId = absentReservationId,
                    session = absentSession,
                    usedPass = pass,
                ),
                ReservationDetail.Attended(
                    reservationId = attendedReservationId,
                    session = attendedSession,
                    checkedInAt = Instant.parse("2026-08-01T00:58:00Z"),
                    usedPass = pass,
                ),
                ReservationDetail.Cancelled(
                    reservationId = cancelledReservationId,
                    session = cancelledSession,
                    cancelledAt = Instant.parse("2026-07-25T04:20:00Z"),
                ),
                ReservationDetail.ClassCancelled(
                    reservationId = classCancelledReservationId,
                    session = classCancelledSession,
                    cancelledAt = Instant.parse("2026-07-24T04:20:00Z"),
                ),
            ),
        waitlistDetails =
            listOf(
                WaitlistDetail(
                    waitlistId = waitlistId,
                    session = waitlistSession,
                    appliedAt = waitlistAppliedAt,
                    currentPosition = 2,
                    pass = passAvailability,
                    cancellation = WaitlistCancellationAvailability.Available,
                ),
                WaitlistDetail(
                    waitlistId = approvalWaitlistId,
                    session = approvalWaitlistSession,
                    appliedAt = approvalWaitlistAppliedAt,
                    currentPosition = 0,
                    pass = passAvailability,
                    cancellation = WaitlistCancellationAvailability.Available,
                ),
                WaitlistDetail(
                    waitlistId = positionOneWaitlistId,
                    session = positionOneWaitlistSession,
                    appliedAt = positionOneWaitlistAppliedAt,
                    currentPosition = 1,
                    pass = passAvailability,
                    cancellation = WaitlistCancellationAvailability.Available,
                ),
            ),
        cancelledAt = Instant.parse("2026-08-18T01:30:00Z"),
        reservationRestoration =
            PassRestoration(
                restoredUses = 1,
                remainingUsesAfterCancellation = 5,
            ),
    )
}

private fun createSession(
    id: String,
    title: String,
    startsAt: String,
    endsAt: String,
    instructorName: String,
    facilityName: String,
    memo: String?,
): ClassSession =
    ClassSession(
        id = ClassSessionId(id),
        title = title,
        period =
            ClassPeriod(
                startsAt = Instant.parse(startsAt),
                endsAt = Instant.parse(endsAt),
                timeZoneId = "Asia/Seoul",
            ),
        instructor =
            InstructorSummary(
                id = InstructorId("instructor-$id"),
                name = instructorName,
                profileImageUrl = null,
            ),
        facility =
            FacilitySummary(
                id = FacilityId("facility-$id"),
                name = facilityName,
            ),
        memo = memo,
    )
