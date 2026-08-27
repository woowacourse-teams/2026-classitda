package com.classitda.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class InstructorDailySessionResponseDto(
    val id: Long,
    val instructorMembershipId: Long,
    val instructorName: String,
    val classForm: ClassFormDto,
    val classType: ClassTypeResponseDto,
    val className: String,
    val description: String? = null,
    val capacity: Int,
    val reservedCount: Long,
    val waitingCount: Long,
    val startAt: String,
    val endAt: String,
    val status: ClassSessionStatusDto,
    val mine: Boolean,
)

@Serializable
internal data class ClassSessionCreateRequestDto(
    val classForm: ClassFormDto,
    val classTypeId: Long,
    val className: String,
    val capacity: Int,
    val durationMinutes: Int,
    val recurring: Boolean,
    val startTime: String,
    val description: String? = null,
    val classDate: String? = null,
    val recurringDays: List<RecurringDayDto>? = null,
    val repeatStartDate: String? = null,
    val repeatEndDate: String? = null,
)

@Serializable
internal data class ClassSessionUpdateRequestDto(
    val classForm: ClassFormDto,
    val classTypeId: Long,
    val className: String,
    val capacity: Int,
    val durationMinutes: Int,
    val startAt: String,
    val description: String? = null,
)

@Serializable
internal enum class ClassSessionStatusDto {
    SCHEDULED_BOOKING_OPEN,
    SCHEDULED_BOOKING_CLOSED,
    IN_PROGRESS,
    COMPLETED,
    CANCELED,
}
