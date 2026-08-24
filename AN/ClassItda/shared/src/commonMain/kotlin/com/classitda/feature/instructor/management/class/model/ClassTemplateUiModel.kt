package com.classitda.feature.instructor.management.`class`.model

internal data class ClassTemplateUiModel(
    val id: String,
    val tags: List<String>,
    val title: String,
    val durationText: String,
    val capacityText: String,
    val schedule: ClassScheduleUiModel? = null,
)
