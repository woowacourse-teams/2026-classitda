package com.classitda.data.repository.student.myschedule

import com.classitda.domain.model.student.myschedule.PassRestoration
import com.classitda.domain.model.student.myschedule.ReservationCancellationReceipt
import com.classitda.domain.model.student.myschedule.ReservationDetail
import com.classitda.domain.model.student.myschedule.ReservationId
import com.classitda.domain.model.student.myschedule.UpcomingSchedule
import com.classitda.domain.model.student.myschedule.UsageHistoryEntry
import com.classitda.domain.model.student.myschedule.UsageHistoryStatus
import com.classitda.domain.model.student.myschedule.WaitlistCancellationReceipt
import com.classitda.domain.model.student.myschedule.WaitlistDetail
import com.classitda.domain.model.student.myschedule.WaitlistId
import com.classitda.domain.repository.student.myschedule.MyScheduleFailureReason
import com.classitda.domain.repository.student.myschedule.MyScheduleRepository
import com.classitda.domain.repository.student.myschedule.MyScheduleResult
import kotlin.time.Instant

internal data class FakeMyScheduleFailures(
    val upcomingSchedules: MyScheduleFailureReason? = null,
    val usageHistory: MyScheduleFailureReason? = null,
    val reservationDetail: MyScheduleFailureReason? = null,
    val waitlistDetail: MyScheduleFailureReason? = null,
    val reservationCancellation: MyScheduleFailureReason? = null,
    val waitlistCancellation: MyScheduleFailureReason? = null,
)

internal class FakeMyScheduleRepository(
    upcomingSchedules: List<UpcomingSchedule> = emptyList(),
    usageHistory: List<UsageHistoryEntry> = emptyList(),
    reservationDetails: List<ReservationDetail> = emptyList(),
    waitlistDetails: List<WaitlistDetail> = emptyList(),
    failures: FakeMyScheduleFailures = FakeMyScheduleFailures(),
    private val cancelledAt: Instant = Instant.parse("2026-08-18T00:00:00Z"),
    private val reservationRestoration: PassRestoration =
        PassRestoration(
            restoredUses = 0,
            remainingUsesAfterCancellation = 0,
        ),
) : MyScheduleRepository {
    private val storedUpcomingSchedules = upcomingSchedules.toMutableList()
    private val storedUsageHistory = usageHistory.toMutableList()
    private val storedReservationDetails =
        reservationDetails.associateByTo(mutableMapOf(), ReservationDetail::reservationId)
    private val storedWaitlistDetails =
        waitlistDetails.associateByTo(mutableMapOf(), WaitlistDetail::waitlistId)

    var failures: FakeMyScheduleFailures = failures

    var lastReservationDetailRequestId: ReservationId? = null
        private set

    var lastWaitlistDetailRequestId: WaitlistId? = null
        private set

    var lastReservationCancellationRequestId: ReservationId? = null
        private set

    var lastWaitlistCancellationRequestId: WaitlistId? = null
        private set

    val upcomingSchedulesSnapshot: List<UpcomingSchedule>
        get() = storedUpcomingSchedules.toList()

    val usageHistorySnapshot: List<UsageHistoryEntry>
        get() = storedUsageHistory.toList()

    override suspend fun getUpcomingSchedules(): MyScheduleResult<List<UpcomingSchedule>> =
        failures.upcomingSchedules.toFailureOrNull()
            ?: MyScheduleResult.Success(upcomingSchedulesSnapshot)

    override suspend fun getUsageHistory(): MyScheduleResult<List<UsageHistoryEntry>> =
        failures.usageHistory.toFailureOrNull()
            ?: MyScheduleResult.Success(usageHistorySnapshot)

    override suspend fun getReservationDetail(reservationId: ReservationId): MyScheduleResult<ReservationDetail> {
        lastReservationDetailRequestId = reservationId
        failures.reservationDetail.toFailureOrNull()?.let { return it }

        return storedReservationDetails[reservationId]
            ?.let { detail -> MyScheduleResult.Success(detail) }
            ?: MyScheduleResult.Failure(MyScheduleFailureReason.NOT_FOUND)
    }

    override suspend fun getWaitlistDetail(waitlistId: WaitlistId): MyScheduleResult<WaitlistDetail> {
        lastWaitlistDetailRequestId = waitlistId
        failures.waitlistDetail.toFailureOrNull()?.let { return it }

        return storedWaitlistDetails[waitlistId]
            ?.let { detail -> MyScheduleResult.Success(detail) }
            ?: MyScheduleResult.Failure(MyScheduleFailureReason.NOT_FOUND)
    }

    override suspend fun cancelReservation(
        reservationId: ReservationId,
    ): MyScheduleResult<ReservationCancellationReceipt> {
        lastReservationCancellationRequestId = reservationId
        failures.reservationCancellation.toFailureOrNull()?.let { return it }

        val confirmedDetail =
            storedReservationDetails[reservationId]
                ?: return MyScheduleResult.Failure(MyScheduleFailureReason.NOT_FOUND)
        if (confirmedDetail !is ReservationDetail.Confirmed) {
            return MyScheduleResult.Failure(MyScheduleFailureReason.CANCELLATION_NOT_ALLOWED)
        }
        val hasUpcomingReservation =
            storedUpcomingSchedules.any { schedule ->
                schedule is UpcomingSchedule.ConfirmedReservation &&
                    schedule.reservationId == reservationId
            }
        if (!hasUpcomingReservation) {
            return MyScheduleResult.Failure(MyScheduleFailureReason.NOT_FOUND)
        }

        storedUpcomingSchedules.removeAll { schedule ->
            schedule is UpcomingSchedule.ConfirmedReservation &&
                schedule.reservationId == reservationId
        }
        storedUsageHistory.removeAll { history -> history.reservationId == reservationId }
        storedUsageHistory +=
            UsageHistoryEntry(
                reservationId = reservationId,
                session = confirmedDetail.session,
                status = UsageHistoryStatus.RESERVATION_CANCELLED,
            )
        storedReservationDetails[reservationId] =
            ReservationDetail.Cancelled(
                reservationId = reservationId,
                session = confirmedDetail.session,
                cancelledAt = cancelledAt,
            )

        return MyScheduleResult.Success(
            ReservationCancellationReceipt(
                reservationId = reservationId,
                session = confirmedDetail.session,
                cancelledAt = cancelledAt,
                restoration = reservationRestoration,
            ),
        )
    }

    override suspend fun cancelWaitlist(waitlistId: WaitlistId): MyScheduleResult<WaitlistCancellationReceipt> {
        lastWaitlistCancellationRequestId = waitlistId
        failures.waitlistCancellation.toFailureOrNull()?.let { return it }

        val detail =
            storedWaitlistDetails[waitlistId]
                ?: return MyScheduleResult.Failure(MyScheduleFailureReason.NOT_FOUND)
        val hasUpcomingWaitlist =
            storedUpcomingSchedules.any { schedule ->
                schedule is UpcomingSchedule.Waitlisted && schedule.waitlistId == waitlistId
            }
        if (!hasUpcomingWaitlist) {
            return MyScheduleResult.Failure(MyScheduleFailureReason.NOT_FOUND)
        }

        storedUpcomingSchedules.removeAll { schedule ->
            schedule is UpcomingSchedule.Waitlisted && schedule.waitlistId == waitlistId
        }
        storedWaitlistDetails.remove(waitlistId)

        return MyScheduleResult.Success(
            WaitlistCancellationReceipt(
                waitlistId = waitlistId,
                session = detail.session,
                cancelledAt = cancelledAt,
                positionAtCancellation = detail.currentPosition,
            ),
        )
    }

    private fun MyScheduleFailureReason?.toFailureOrNull(): MyScheduleResult.Failure? =
        this?.let { reason -> MyScheduleResult.Failure(reason) }
}
