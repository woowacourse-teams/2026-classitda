package com.classitda.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class ClassTemplateResponseDto(
    val id: Long,
    val name: String,
    val description: String? = null,
    val classForm: ClassFormDto,
    val durationMinutes: Int,
    val startTime: String,
    val recurringDays: List<RecurringDayDto> = emptyList(),
    val capacity: Int,
    val classTypes: List<ClassTypeResponseDto> = emptyList(),
)

@Serializable
internal data class ClassTemplateCreateRequestDto(
    val name: String,
    val description: String? = null,
    val classForm: ClassFormDto,
    val durationMinutes: Int,
    val startTime: String,
    val recurringDays: List<RecurringDayDto> = emptyList(),
    val capacity: Int,
    val classTypeIds: List<Long>,
)

@Serializable
internal data class ClassTemplateUpdateRequestDto(
    val name: String,
    val description: String? = null,
    val classForm: ClassFormDto,
    val durationMinutes: Int,
    val startTime: String,
    val recurringDays: List<RecurringDayDto> = emptyList(),
    val capacity: Int,
    val classTypeIds: List<Long>,
)

@Serializable
internal enum class ClassFormDto { INDIVIDUAL, GROUP }

@Serializable
internal enum class RecurringDayDto { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY }
