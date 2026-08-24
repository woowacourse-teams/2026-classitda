package com.classitda.feature.instructor.management.`class`.create.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

internal data class ClassTemplateFormValues(
    val classType: ClassType,
    val categories: List<String>,
    val title: String,
    val capacity: Int,
    val durationMinutes: Int,
    val isRepeating: Boolean,
    val repeatDays: Set<DayOfWeek>,
    val startTime: LocalTime,
    val description: String,
)
