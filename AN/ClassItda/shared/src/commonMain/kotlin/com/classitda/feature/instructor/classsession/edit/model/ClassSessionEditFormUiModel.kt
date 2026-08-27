package com.classitda.feature.instructor.classsession.edit.model

import com.classitda.feature.instructor.management.model.ClassFormOption
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

internal data class ClassSessionEditFormUiModel(
    val id: String,
    val classTypeId: String,
    val classType: ClassFormOption,
    val categories: List<String>,
    val title: String,
    val capacity: Int,
    val reservedCount: Int,
    val durationMinutes: Int,
    val startTime: LocalTime,
    val sessionDate: LocalDate,
    val description: String,
)
