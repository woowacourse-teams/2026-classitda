package com.classitda.feature.student.myschedule.mapper

import com.classitda.domain.model.student.myschedule.CancellationUnavailableReason
import com.classitda.domain.model.student.myschedule.ClassSession
import com.classitda.domain.model.student.myschedule.MemberPassAvailability
import com.classitda.domain.model.student.myschedule.MemberPassSummary
import com.classitda.domain.model.student.myschedule.ReservationCancellationAvailability
import com.classitda.domain.model.student.myschedule.ReservationCancellationReceipt
import com.classitda.domain.model.student.myschedule.ReservationDetail
import com.classitda.domain.model.student.myschedule.UpcomingSchedule
import com.classitda.domain.model.student.myschedule.UsageHistoryEntry
import com.classitda.domain.model.student.myschedule.UsageHistoryStatus
import com.classitda.domain.model.student.myschedule.WaitlistCancellationAvailability
import com.classitda.domain.model.student.myschedule.WaitlistCancellationReceipt
import com.classitda.domain.model.student.myschedule.WaitlistDetail
import com.classitda.feature.student.myschedule.contract.ReservationCancellationAvailabilityUiModel
import com.classitda.feature.student.myschedule.contract.ReservationCancellationResultUiModel
import com.classitda.feature.student.myschedule.contract.ReservationCancellationUnavailableReasonUiModel
import com.classitda.feature.student.myschedule.contract.ReservationClassInfoUiModel
import com.classitda.feature.student.myschedule.contract.ReservationDetailUiModel
import com.classitda.feature.student.myschedule.contract.ReservationPassAvailabilityUiModel
import com.classitda.feature.student.myschedule.contract.ReservationUsedPassUiModel
import com.classitda.feature.student.myschedule.contract.UpcomingDateSectionUiModel
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleCardUiModel
import com.classitda.feature.student.myschedule.contract.UsageHistoryCardUiModel
import com.classitda.feature.student.myschedule.contract.UsageHistoryMonthSectionUiModel
import com.classitda.feature.student.myschedule.contract.UsageHistoryStatusUiModel
import com.classitda.feature.student.myschedule.contract.WaitlistCancellationAvailabilityUiModel
import com.classitda.feature.student.myschedule.contract.WaitlistCancellationResultUiModel
import com.classitda.feature.student.myschedule.contract.WaitlistCancellationUnavailableReasonUiModel
import com.classitda.feature.student.myschedule.contract.WaitlistClassInfoUiModel
import com.classitda.feature.student.myschedule.contract.WaitlistDetailUiModel
import com.classitda.feature.student.myschedule.contract.WaitlistPassAvailabilityUiModel
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

internal enum class MyScheduleDisplayLocale {
    KOREAN,
}

internal class MyScheduleUiMapper(
    private val locale: MyScheduleDisplayLocale,
    private val currentTimeProvider: CurrentTimeProvider,
) {
    fun mapUpcomingSchedules(schedules: List<UpcomingSchedule>): List<UpcomingDateSectionUiModel> =
        schedules
            .sortedBy { it.session.period.startsAt }
            .groupBy { schedule -> schedule.session.localStart().date }
            .map { (date, schedulesOnDate) ->
                UpcomingDateSectionUiModel(
                    dateLabel = formatUpcomingSectionDate(date),
                    items = schedulesOnDate.map(::mapUpcomingSchedule),
                )
            }

    fun mapUsageHistory(history: List<UsageHistoryEntry>): List<UsageHistoryMonthSectionUiModel> {
        val entriesByMonth =
            history
                .sortedByDescending { it.session.period.startsAt }
                .groupBy { entry ->
                    entry.session
                        .localStart()
                        .date
                        .toYearMonthKey()
                }

        return entriesByMonth.map { (yearMonth, entriesInMonth) ->
            UsageHistoryMonthSectionUiModel(
                monthLabel = formatMonth(yearMonth),
                items = entriesInMonth.map(::mapUsageHistoryEntry),
            )
        }
    }

    fun mapReservationDetail(
        detail: ReservationDetail,
        cancellationDeadlineHoursBeforeStart: Int,
    ): ReservationDetailUiModel =
        when (detail) {
            is ReservationDetail.Confirmed -> {
                ReservationDetailUiModel.Confirmed(
                    reservationId = detail.reservationId,
                    title = detail.session.title,
                    classInfo = detail.session.toReservationClassInfo(useLongDate = false),
                    reservedAtLabel = detail.session.formatInstant(detail.reservedAt),
                    pass = detail.pass.toReservationPassUiModel(),
                    cancellationDeadlineHoursBeforeStart = cancellationDeadlineHoursBeforeStart,
                    cancellation = detail.cancellation.toReservationCancellationUiModel(detail.session.period.startsAt),
                )
            }

            is ReservationDetail.Cancelled -> {
                ReservationDetailUiModel.Cancelled(
                    reservationId = detail.reservationId,
                    title = detail.session.title,
                    classInfo = detail.session.toReservationClassInfo(useLongDate = false),
                    cancelledAtLabel = detail.session.formatInstant(detail.cancelledAt),
                )
            }

            is ReservationDetail.Attended -> {
                ReservationDetailUiModel.Attended(
                    reservationId = detail.reservationId,
                    title = detail.session.title,
                    classInfo = detail.session.toReservationClassInfo(useLongDate = true),
                    checkedInAtLabel = detail.session.formatInstant(detail.checkedInAt),
                    usedPass = detail.usedPass.toReservationUsedPassUiModel(),
                )
            }

            is ReservationDetail.Absent -> {
                ReservationDetailUiModel.Absent(
                    reservationId = detail.reservationId,
                    title = detail.session.title,
                    classInfo = detail.session.toReservationClassInfo(useLongDate = false),
                    attendanceTimePlaceholder = ABSENT_ATTENDANCE_TIME_PLACEHOLDER,
                    usedPass = detail.usedPass.toReservationUsedPassUiModel(),
                )
            }
        }

    fun mapWaitlistDetail(detail: WaitlistDetail): WaitlistDetailUiModel =
        WaitlistDetailUiModel(
            waitlistId = detail.waitlistId,
            title = detail.session.title,
            appliedAtLabel = detail.session.formatInstant(detail.appliedAt),
            currentPosition = detail.currentPosition,
            classInfo = detail.session.toWaitlistClassInfo(),
            pass = detail.pass.toWaitlistPassUiModel(),
            cancellation = mapWaitlistCancellationAvailability(detail.cancellation),
        )

    fun mapReservationCancellationReceipt(
        receipt: ReservationCancellationReceipt,
    ): ReservationCancellationResultUiModel =
        ReservationCancellationResultUiModel(
            reservationId = receipt.reservationId,
            title = receipt.session.title,
            classInfo = receipt.session.toReservationClassInfo(useLongDate = false),
            cancelledAtLabel = receipt.session.formatInstant(receipt.cancelledAt),
            restoredPassUses = receipt.restoration.restoredUses,
        )

    fun mapWaitlistCancellationReceipt(receipt: WaitlistCancellationReceipt): WaitlistCancellationResultUiModel {
        val start = receipt.session.localStart()
        val end = receipt.session.localEnd()

        return WaitlistCancellationResultUiModel(
            waitlistId = receipt.waitlistId,
            title = receipt.session.title,
            instructorName = receipt.session.instructor.name,
            dateLabel = formatDotDate(start.date),
            timeRangeLabel = formatTimeRange(start, end),
            cancelledAtLabel = receipt.session.formatInstant(receipt.cancelledAt),
            positionAtCancellation = receipt.positionAtCancellation,
        )
    }

    private fun mapUpcomingSchedule(schedule: UpcomingSchedule): UpcomingScheduleCardUiModel {
        val start = schedule.session.localStart()
        val end = schedule.session.localEnd()
        val timeRangeLabel = formatTimeRange(start, end)

        return when (schedule) {
            is UpcomingSchedule.ConfirmedReservation -> {
                UpcomingScheduleCardUiModel.ConfirmedReservation(
                    reservationId = schedule.reservationId,
                    timeRangeLabel = timeRangeLabel,
                    title = schedule.session.title,
                    instructorName = schedule.session.instructor.name,
                )
            }

            is UpcomingSchedule.Waitlisted -> {
                UpcomingScheduleCardUiModel.Waitlisted(
                    waitlistId = schedule.waitlistId,
                    timeRangeLabel = timeRangeLabel,
                    title = schedule.session.title,
                    instructorName = schedule.session.instructor.name,
                )
            }
        }
    }

    private fun mapUsageHistoryEntry(entry: UsageHistoryEntry): UsageHistoryCardUiModel {
        val start = entry.session.localStart()
        val end = entry.session.localEnd()

        return UsageHistoryCardUiModel(
            reservationId = entry.reservationId,
            dateTimeLabel = "${formatDotDate(start.date)} ${formatTimeRange(start, end)}",
            title = entry.session.title,
            instructorName = entry.session.instructor.name,
            status = entry.status.toUiModel(),
        )
    }

    private fun ClassSession.toReservationClassInfo(useLongDate: Boolean): ReservationClassInfoUiModel {
        val start = localStart()
        val end = localEnd()

        return ReservationClassInfoUiModel(
            dateLabel = if (useLongDate) formatLongDate(start.date) else formatDotDate(start.date),
            timeRangeLabel = formatTimeRange(start, end),
            memo = memo,
            instructorName = instructor.name,
            facilityName = facility.name,
        )
    }

    private fun ClassSession.toWaitlistClassInfo(): WaitlistClassInfoUiModel {
        val start = localStart()
        val end = localEnd()

        return WaitlistClassInfoUiModel(
            dateLabel = formatDotDate(start.date),
            timeRangeLabel = formatTimeRange(start, end),
            memo = memo,
            instructorName = instructor.name,
            facilityName = facility.name,
        )
    }

    private fun MemberPassAvailability.toReservationPassUiModel(): ReservationPassAvailabilityUiModel =
        ReservationPassAvailabilityUiModel(
            name = pass.name,
            validityLabel = formatValidity(pass),
            remainingUses = remainingUses,
            reservableUses = reservableUses,
            cancellableUses = cancellableUses,
        )

    private fun MemberPassAvailability.toWaitlistPassUiModel(): WaitlistPassAvailabilityUiModel =
        WaitlistPassAvailabilityUiModel(
            name = pass.name,
            validityLabel = formatValidity(pass),
            remainingUses = remainingUses,
            reservableUses = reservableUses,
            cancellableUses = cancellableUses,
        )

    private fun MemberPassSummary.toReservationUsedPassUiModel(): ReservationUsedPassUiModel =
        ReservationUsedPassUiModel(
            name = name,
            validityLabel = formatValidity(this),
        )

    private fun ReservationCancellationAvailability.toReservationCancellationUiModel(
        startsAt: Instant,
    ): ReservationCancellationAvailabilityUiModel =
        when (this) {
            is ReservationCancellationAvailability.Available -> {
                ReservationCancellationAvailabilityUiModel.Available(
                    hoursUntilStart = hoursUntil(startsAt),
                    restoredPassUses = restoredPassUses,
                )
            }

            is ReservationCancellationAvailability.Unavailable -> {
                ReservationCancellationAvailabilityUiModel.Unavailable(reason.toReservationUiModel())
            }
        }

    private fun mapWaitlistCancellationAvailability(
        availability: WaitlistCancellationAvailability,
    ): WaitlistCancellationAvailabilityUiModel =
        when (availability) {
            WaitlistCancellationAvailability.Available -> {
                WaitlistCancellationAvailabilityUiModel.Available
            }

            is WaitlistCancellationAvailability.Unavailable -> {
                WaitlistCancellationAvailabilityUiModel.Unavailable(availability.reason.toWaitlistUiModel())
            }
        }

    private fun CancellationUnavailableReason.toReservationUiModel(): ReservationCancellationUnavailableReasonUiModel =
        when (this) {
            CancellationUnavailableReason.DEADLINE_PASSED -> {
                ReservationCancellationUnavailableReasonUiModel.DEADLINE_PASSED
            }

            CancellationUnavailableReason.NO_REMAINING_CANCELLATION -> {
                ReservationCancellationUnavailableReasonUiModel.NO_REMAINING_CANCELLATION
            }

            CancellationUnavailableReason.ALREADY_CANCELLED -> {
                ReservationCancellationUnavailableReasonUiModel.ALREADY_CANCELLED
            }

            CancellationUnavailableReason.UNKNOWN -> {
                ReservationCancellationUnavailableReasonUiModel.UNKNOWN
            }
        }

    private fun CancellationUnavailableReason.toWaitlistUiModel(): WaitlistCancellationUnavailableReasonUiModel =
        when (this) {
            CancellationUnavailableReason.DEADLINE_PASSED -> {
                WaitlistCancellationUnavailableReasonUiModel.DEADLINE_PASSED
            }

            CancellationUnavailableReason.NO_REMAINING_CANCELLATION -> {
                WaitlistCancellationUnavailableReasonUiModel.NO_REMAINING_CANCELLATION
            }

            CancellationUnavailableReason.ALREADY_CANCELLED -> {
                WaitlistCancellationUnavailableReasonUiModel.ALREADY_CANCELLED
            }

            CancellationUnavailableReason.UNKNOWN -> {
                WaitlistCancellationUnavailableReasonUiModel.UNKNOWN
            }
        }

    private fun UsageHistoryStatus.toUiModel(): UsageHistoryStatusUiModel =
        when (this) {
            UsageHistoryStatus.ATTENDED -> UsageHistoryStatusUiModel.ATTENDED
            UsageHistoryStatus.ABSENT -> UsageHistoryStatusUiModel.ABSENT
            UsageHistoryStatus.RESERVATION_CANCELLED -> UsageHistoryStatusUiModel.RESERVATION_CANCELLED
        }

    private fun ClassSession.localStart(): LocalDateTime =
        period.startsAt.toLocalDateTime(TimeZone.of(period.timeZoneId))

    private fun ClassSession.localEnd(): LocalDateTime = period.endsAt.toLocalDateTime(TimeZone.of(period.timeZoneId))

    private fun ClassSession.formatInstant(instant: Instant): String {
        val dateTime = instant.toLocalDateTime(TimeZone.of(period.timeZoneId))
        return "${formatDotDate(dateTime.date)} ${formatPeriodTime(dateTime)}"
    }

    private fun formatValidity(pass: MemberPassSummary): String =
        "${formatPlainDate(pass.validFrom)} ~ ${formatPlainDate(pass.validUntil)}"

    private fun formatUpcomingSectionDate(date: LocalDate): String =
        when (locale) {
            MyScheduleDisplayLocale.KOREAN -> {
                "${date.month.number}월 ${date.day}일 ${formatWeekday(date.dayOfWeek, full = true)}"
            }
        }

    private fun formatMonth(yearMonth: YearMonthKey): String =
        when (locale) {
            MyScheduleDisplayLocale.KOREAN -> "${yearMonth.year}년 ${yearMonth.month}월"
        }

    private fun formatDotDate(date: LocalDate): String =
        "${formatPlainDate(date)} (${formatWeekday(date.dayOfWeek, full = false)})"

    private fun formatLongDate(date: LocalDate): String =
        when (locale) {
            MyScheduleDisplayLocale.KOREAN -> {
                "${date.year}년 ${date.month.number}월 ${date.day}일 ${formatWeekday(date.dayOfWeek, full = true)}"
            }
        }

    private fun formatPlainDate(date: LocalDate): String =
        "${date.year}.${date.month.number.padToTwoDigits()}.${date.day.padToTwoDigits()}"

    private fun formatTimeRange(
        start: LocalDateTime,
        end: LocalDateTime,
    ): String {
        val startPeriod = periodOf(start.hour)
        val endPeriod = periodOf(end.hour)
        val startLabel = formatClockTime(start)
        val endLabel = formatClockTime(end)

        return if (startPeriod == endPeriod) {
            "$startPeriod $startLabel ~ $endLabel"
        } else {
            "$startPeriod $startLabel ~ $endPeriod $endLabel"
        }
    }

    private fun formatPeriodTime(dateTime: LocalDateTime): String =
        "${periodOf(dateTime.hour)} ${formatClockTime(dateTime)}"

    private fun periodOf(hour: Int): String =
        when (locale) {
            MyScheduleDisplayLocale.KOREAN -> if (hour < 12) "오전" else "오후"
        }

    private fun formatClockTime(dateTime: LocalDateTime): String {
        val hour = (dateTime.hour % 12).takeUnless { it == 0 } ?: 12
        return "$hour:${dateTime.minute.padToTwoDigits()}"
    }

    private fun formatWeekday(
        dayOfWeek: DayOfWeek,
        full: Boolean,
    ): String {
        val shortLabel =
            when (locale) {
                MyScheduleDisplayLocale.KOREAN -> {
                    when (dayOfWeek) {
                        DayOfWeek.MONDAY -> "월"
                        DayOfWeek.TUESDAY -> "화"
                        DayOfWeek.WEDNESDAY -> "수"
                        DayOfWeek.THURSDAY -> "목"
                        DayOfWeek.FRIDAY -> "금"
                        DayOfWeek.SATURDAY -> "토"
                        DayOfWeek.SUNDAY -> "일"
                    }
                }
            }

        return if (full) "${shortLabel}요일" else shortLabel
    }

    private fun hoursUntil(startsAt: Instant): Int =
        (startsAt - currentTimeProvider.now())
            .inWholeHours
            .coerceAtLeast(0L)
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()

    private fun LocalDate.toYearMonthKey(): YearMonthKey =
        YearMonthKey(
            year = year,
            month = month.number,
        )

    private fun Int.padToTwoDigits(): String = toString().padStart(length = 2, padChar = '0')

    private data class YearMonthKey(
        val year: Int,
        val month: Int,
    )

    private companion object {
        const val ABSENT_ATTENDANCE_TIME_PLACEHOLDER = "--:--:--"
    }
}
