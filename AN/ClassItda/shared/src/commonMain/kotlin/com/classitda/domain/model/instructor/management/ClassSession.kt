package com.classitda.domain.model.instructor.management

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

data class ClassSession(
    val id: String,
    val classTypeId: String,
    val tags: List<String>,
    val title: String,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val reservedCount: Int,
    val capacity: Int,
    val status: ClassSessionStatus,
    val members: List<ClassSessionMember> = emptyList(),
)

data class ClassSessionCreateRequest(
    val classForm: ClassForm,
    val classTypeId: String,
    val title: String,
    val capacity: Int,
    val durationMinutes: Int,
    val startTime: LocalTime,
    val description: String,
    val recurring: Boolean,
    val classDate: LocalDate? = null,
    val recurringDays: List<DayOfWeek> = emptyList(),
    val repeatStartDate: LocalDate? = null,
    val repeatEndDate: LocalDate? = null,
)
