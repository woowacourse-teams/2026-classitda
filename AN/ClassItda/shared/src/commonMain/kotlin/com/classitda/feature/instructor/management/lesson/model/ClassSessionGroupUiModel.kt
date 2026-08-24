package com.classitda.feature.instructor.management.lesson.model

internal data class ClassSessionGroupUiModel(
    val dateText: String,
    val sessions: List<ClassSessionUiModel>,
)
