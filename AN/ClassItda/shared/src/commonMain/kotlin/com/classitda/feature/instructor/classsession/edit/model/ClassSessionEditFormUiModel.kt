package com.classitda.feature.instructor.classsession.edit.model

import com.classitda.feature.instructor.management.lesson.create.model.ClassType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

internal data class ClassSessionEditFormUiModel(
    val id: String,
    val classTypeId: String,
    val classType: ClassType,
    val categories: List<String>,
    val title: String,
    val capacity: Int,
    val reservedCount: Int,
    val durationMinutes: Int,
    val startTime: LocalTime,
    val sessionDate: LocalDate,
    val description: String,
)
