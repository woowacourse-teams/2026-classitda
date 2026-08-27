package com.classitda.feature.instructor.component

import com.classitda.domain.model.instructor.management.ClassSession
import kotlinx.datetime.LocalDateTime

internal fun ClassSession.instructorTimeText(): String {
    val startPeriod = startAt.periodText()
    val endPeriod = endAt.periodText()
    val startTime = startAt.clockText()
    val endTime = endAt.clockText()

    return if (startPeriod == endPeriod) {
        "$startPeriod $startTime ~ $endTime"
    } else {
        "$startPeriod $startTime ~ $endPeriod $endTime"
    }
}

private fun LocalDateTime.periodText(): String = if (hour < 12) "오전" else "오후"

private fun LocalDateTime.clockText(): String {
    val displayHour = hour % 12
    val normalizedHour = if (displayHour == 0) 12 else displayHour
    return "$normalizedHour:${minute.toString().padStart(2, '0')}"
}
