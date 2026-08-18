package com.classitda.feature.student.home.util

import com.classitda.domain.model.home.FacilityNotice
import com.classitda.domain.model.home.Pass
import com.classitda.domain.model.home.PendingReservation
import com.classitda.domain.model.home.UpcomingReservation
import com.classitda.feature.student.home.model.FacilityNoticeUiModel
import com.classitda.feature.student.home.model.MyPassUiModel
import com.classitda.feature.student.home.model.PendingReservationUiModel
import com.classitda.feature.student.home.model.UpcomingReservationUiModel
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Instant

private const val APPROVAL_WINDOW_MINUTES = 60

internal fun PendingReservation.toUiModel(
    now: Instant,
    timeZone: TimeZone,
): PendingReservationUiModel {
    val remainingMin = (startAt.toInstant(timeZone) - now).inWholeMinutes.toInt().coerceAtLeast(0)
    return PendingReservationUiModel(
        reservationId = id,
        className = className,
        instructorName = instructorName,
        classTimeText = "${dayPrefix(startAt.date, now, timeZone)} ${formatAmPmTime(startAt.time)}",
        remainingMin = remainingMin,
        remainingProgress = remainingMin.coerceIn(0, APPROVAL_WINDOW_MINUTES) / APPROVAL_WINDOW_MINUTES.toFloat(),
        dateText = formatDateWithDayOfWeek(startAt.date),
        timeRangeText = "${formatAmPmTime(startAt.time)} ~ ${formatAmPmTime(endAt.time)}",
        memo = memo,
        passName = pass.passName,
        totalRemainingCount = pass.totalRemainingCount,
        reservableCount = pass.reservableCount,
        cancellableCount = pass.cancellableCount,
    )
}

internal fun UpcomingReservation.toUiModel(
    now: Instant,
    timeZone: TimeZone,
): UpcomingReservationUiModel {
    val remaining = startAt.toInstant(timeZone) - now
    return UpcomingReservationUiModel(
        classDateTimeText =
            "${startAt.date.month.number}월 ${startAt.date.day}일 · " +
                "${startAt.date.dayOfWeek.toKoreanFull()} ${formatAmPmTime(startAt.time)}",
        className = className,
        instructorName = instructorName,
        memo = memo,
        remainingTimeText = formatRemainingTime(remaining),
    )
}

internal fun Pass.toUiModel(): MyPassUiModel =
    MyPassUiModel(
        passName = name,
        expireDateText = "${formatDateDot(expireDate)}까지",
        totalRemaining = totalRemainingCount,
        reservable = reservableCount,
        cancellable = cancellableCount,
        holdingPeriodText =
            holdingPeriod?.let {
                "${formatDateDot(it.startDate)} ~ ${formatDateDot(it.endDate)}"
            },
    )

internal fun FacilityNotice.toUiModel(): FacilityNoticeUiModel =
    FacilityNoticeUiModel(
        title = title,
        description = description,
        dateText = formatDateDot(postedDate),
    )

private fun dayPrefix(
    date: LocalDate,
    now: Instant,
    timeZone: TimeZone,
): String {
    val today = now.toLocalDateTime(timeZone).date
    return when (date) {
        today -> "오늘"
        today.plus(1, DateTimeUnit.DAY) -> "내일"
        else -> formatDateDot(date)
    }
}

private fun formatAmPmTime(time: LocalTime): String {
    val amPm = if (time.hour < 12) "오전" else "오후"
    val hour12 =
        when {
            time.hour == 0 -> 12
            time.hour > 12 -> time.hour - 12
            else -> time.hour
        }
    return "$amPm $hour12:${time.minute.toString().padStart(2, '0')}"
}

private fun formatDateDot(date: LocalDate): String =
    "${date.year}.${date.month.number.toString().padStart(2, '0')}.${date.day.toString().padStart(2, '0')}"

private fun formatDateWithDayOfWeek(date: LocalDate): String =
    "${formatDateDot(date)} (${date.dayOfWeek.toKoreanShort()})"

private fun formatRemainingTime(duration: Duration): String {
    val totalMinutes = duration.inWholeMinutes.coerceAtLeast(0)
    return when {
        totalMinutes < 60 -> "${totalMinutes}분 남음"
        totalMinutes < 24 * 60 -> "${(totalMinutes + 30) / 60}시간 남음"
        else -> "${(totalMinutes + 12 * 60) / (24 * 60)}일 남음"
    }
}

private fun DayOfWeek.toKoreanShort(): String =
    when (this) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }

private fun DayOfWeek.toKoreanFull(): String = "${toKoreanShort()}요일"
