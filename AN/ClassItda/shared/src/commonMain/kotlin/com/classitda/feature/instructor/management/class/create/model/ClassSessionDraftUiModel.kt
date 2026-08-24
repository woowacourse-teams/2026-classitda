package com.classitda.feature.instructor.management.`class`.create.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

internal data class ClassSessionDraftUiModel(
    val templateId: String?,
    val classType: ClassType,
    val categories: List<String>,
    val title: String,
    val capacity: Int,
    val durationMinutes: Int,
    val startTime: LocalTime,
    val isRepeating: Boolean,
    val repeatDays: Set<DayOfWeek>,
    val repeatStartDate: LocalDate?,
    val repeatEndDate: LocalDate?,
    val sessionDate: LocalDate?,
    val description: String,
)
