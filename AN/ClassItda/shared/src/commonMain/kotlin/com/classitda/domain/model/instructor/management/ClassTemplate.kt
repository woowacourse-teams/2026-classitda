package com.classitda.domain.model.instructor.management

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

data class ClassTemplate(
    val id: String,
    val tags: List<String>,
    val title: String,
    val durationMinutes: Int,
    val capacity: Int,
    val schedule: ClassTemplateSchedule? = null,
    val description: String = "",
)

data class ClassTemplateSchedule(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val repeatDays: List<DayOfWeek>,
)
