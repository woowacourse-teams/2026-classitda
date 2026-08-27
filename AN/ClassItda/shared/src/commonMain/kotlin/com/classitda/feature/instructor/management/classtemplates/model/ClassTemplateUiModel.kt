package com.classitda.feature.instructor.management.classtemplates.model

import com.classitda.domain.model.instructor.management.ClassForm

internal data class ClassTemplateUiModel(
    val id: String,
    val classForm: ClassForm,
    val classTypeId: String?,
    val categoryNames: List<String>,
    val title: String,
    val durationText: String,
    val capacityText: String,
    val schedule: ClassScheduleUiModel? = null,
)
