package com.classitda.feature.instructor.management.classtemplates.create.model

import com.classitda.feature.instructor.management.model.ClassType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

internal data class ClassTemplateDraftUiModel(
    val classType: ClassType,
    val categories: List<String>,
    val classTypeIds: List<String>,
    val title: String,
    val capacity: Int,
    val durationMinutes: Int,
    val isRepeating: Boolean,
    val repeatDays: Set<DayOfWeek>,
    val startTime: LocalTime,
    val description: String,
)
