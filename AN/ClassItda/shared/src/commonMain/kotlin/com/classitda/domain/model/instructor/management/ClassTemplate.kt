package com.classitda.domain.model.instructor.management

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

data class ClassTemplate(
    val id: String,
    val tags: List<String>,
    val title: String,
    val classForm: ClassForm,
    val durationMinutes: Int,
    val capacity: Int,
    val schedule: ClassTemplateSchedule? = null,
    val description: String = "",
    val classTypeId: String? = null,
)

enum class ClassForm {
    INDIVIDUAL,
    GROUP,
}

data class ClassTemplateSchedule(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val repeatDays: List<DayOfWeek>,
)
