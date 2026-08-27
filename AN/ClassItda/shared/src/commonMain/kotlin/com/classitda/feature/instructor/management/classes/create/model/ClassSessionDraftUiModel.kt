package com.classitda.feature.instructor.management.classes.create.model

import com.classitda.domain.model.instructor.management.ClassType
import com.classitda.feature.instructor.management.model.ClassFormOption
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

internal data class ClassSessionDraftUiModel(
    val templateId: String?,
    val classType: ClassFormOption,
    val category: ClassType?,
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
