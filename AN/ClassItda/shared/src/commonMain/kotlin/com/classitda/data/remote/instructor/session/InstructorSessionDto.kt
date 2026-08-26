package com.classitda.data.remote.instructor.session

import kotlinx.serialization.Serializable

@Serializable
internal data class ClassTypeResponseDto(
    val id: Long,
    val name: String,
)

@Serializable
internal data class InstructorDailySessionResponseDto(
    val id: Long,
    val instructorMembershipId: Long,
    val instructorName: String,
    val classForm: String,
    val classType: ClassTypeResponseDto,
    val className: String,
    val description: String? = null,
    val capacity: Int,
    val reservedCount: Long,
    val waitingCount: Long,
    val startAt: String,
    val endAt: String,
    val status: String,
    val mine: Boolean,
)

@Serializable
internal data class InstructorCalendarResponseDto(
    val date: String,
    val scheduled: Boolean,
    val completed: Boolean,
    val mineScheduled: Boolean,
    val mineCompleted: Boolean,
)

@Serializable
internal data class ClassSessionDetailResponseDto(
    val id: Long,
    val instructorMembershipId: Long,
    val instructorName: String,
    val classForm: String,
    val classType: ClassTypeResponseDto,
    val className: String,
    val description: String? = null,
    val capacity: Int,
    val startAt: String,
    val endAt: String,
    val status: String,
    val mine: Boolean,
    val reservedMembers: List<ReservedMemberResponseDto> = emptyList(),
)

@Serializable
internal data class ReservedMemberResponseDto(
    val enrollmentId: Long,
    val membershipId: Long,
    val name: String,
    val profileImageUrl: String? = null,
)

@Serializable
internal data class ClassSessionUpdateV1RequestDto(
    val classForm: String,
    val classTypeId: Long,
    val className: String,
    val capacity: Int,
    val durationMinutes: Int,
    val startAt: String,
    val description: String? = null,
)
