package com.classitda.feature.instructor.management.lesson.model

import com.classitda.domain.model.instructor.management.ClassSessionStatus

internal data class ClassSessionUiModel(
    val id: String,
    val tags: List<String>,
    val title: String,
    val timeRangeText: String,
    val reservedCount: Int,
    val capacity: Int,
    val status: ClassSessionStatus,
)
