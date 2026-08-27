package com.classitda.feature.instructor.classsession.detail.model

import com.classitda.domain.model.instructor.management.ClassSessionStatus

internal data class ClassSessionDetailUiModel(
    val id: String,
    val dateText: String,
    val tags: List<String>,
    val title: String,
    val timeText: String,
    val reservedCount: Int,
    val capacity: Int,
    val description: String,
    val status: ClassSessionStatus,
    val members: List<ClassSessionMemberUiModel>,
)

internal data class ClassSessionMemberUiModel(
    val id: String,
    val name: String,
    val isTemporary: Boolean = false,
    val enrollmentId: String? = null,
)
