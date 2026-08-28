package com.classitda.feature.instructor.management.classtemplates.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

internal data class ClassScheduleUiModel(
    val startTime: LocalTime,
    val repeatDays: Set<DayOfWeek>,
    val timeRangeText: String,
    val repeatDaysText: String,
)
