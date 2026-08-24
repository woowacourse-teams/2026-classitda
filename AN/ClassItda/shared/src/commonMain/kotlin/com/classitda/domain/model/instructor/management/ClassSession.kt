package com.classitda.domain.model.instructor.management

import kotlinx.datetime.LocalDateTime

data class ClassSession(
    val id: String,
    val tags: List<String>,
    val title: String,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val reservedCount: Int,
    val capacity: Int,
    val status: ClassSessionStatus,
    val members: List<ClassSessionMember> = emptyList(),
)
