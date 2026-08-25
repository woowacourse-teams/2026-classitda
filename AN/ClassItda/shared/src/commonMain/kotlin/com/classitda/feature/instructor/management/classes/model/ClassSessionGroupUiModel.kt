package com.classitda.feature.instructor.management.classes.model

internal data class ClassSessionGroupUiModel(
    val dateText: String,
    val sessions: List<ClassSessionUiModel>,
)
