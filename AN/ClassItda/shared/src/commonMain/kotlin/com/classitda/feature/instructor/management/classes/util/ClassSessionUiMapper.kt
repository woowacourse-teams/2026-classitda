package com.classitda.feature.instructor.management.classes.util

import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.feature.instructor.management.classes.model.ClassSessionGroupUiModel
import com.classitda.feature.instructor.management.classes.model.ClassSessionUiModel
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number

internal fun List<ClassSession>.toSessionGroupUiModels(): List<ClassSessionGroupUiModel> =
    groupBy { it.startAt.date }
        .entries
        .sortedByDescending { it.key }
        .map { (date, sessions) ->
            ClassSessionGroupUiModel(
                dateText = formatSessionDateHeader(date),
                sessions = sessions.sortedBy { it.startAt.time }.map { it.toUiModel() },
            )
        }

private fun ClassSession.toUiModel(): ClassSessionUiModel =
    ClassSessionUiModel(
        id = id,
        tags = tags,
        title = title,
        timeRangeText = "${formatAmPmTime(startAt.time)} ~ ${formatPlainTime(endAt.time)}",
        reservedCount = reservedCount,
        capacity = capacity,
        status = status,
    )

private fun formatSessionDateHeader(date: LocalDate): String =
    "${date.month.number}월 ${date.day}일 ${date.dayOfWeek.toKoreanFull()}"

private fun formatAmPmTime(time: LocalTime): String {
    val amPm = if (time.hour < 12) "오전" else "오후"
    return "$amPm ${hour12(time)}:${time.minute.toString().padStart(2, '0')}"
}

private fun formatPlainTime(time: LocalTime): String = "${hour12(time)}:${time.minute.toString().padStart(2, '0')}"

private fun hour12(time: LocalTime): Int =
    when {
        time.hour == 0 -> 12
        time.hour > 12 -> time.hour - 12
        else -> time.hour
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
