package com.classitda.data.remote.instructor.member

import kotlinx.serialization.Serializable

@Serializable
internal data class CursorResponseDto<T>(
    val items: List<T>,
    val hasNext: Boolean,
    val nextCursor: String? = null,
)

@Serializable
internal data class StudioMembershipResponseDto(
    val id: Long,
    val name: String,
    val phoneNumber: String,
    val registered: Boolean,
    val status: String,
)

@Serializable
internal data class StudioMembershipDetailResponseDto(
    val id: Long,
    val name: String,
    val phoneNumber: String,
    val studioRole: StudioRoleResponseDto,
    val registered: Boolean,
    val status: String,
    val joinedAt: String? = null,
)

@Serializable
internal data class StudioRoleResponseDto(
    val id: Long,
    val name: String,
    val instructor: Boolean,
)

@Serializable
internal data class StudentMembershipCreateRequestDto(
    val name: String,
    val phoneNumber: String,
)

@Serializable
internal data class StudentMembershipUpdateRequestDto(
    val name: String,
    val phoneNumber: String,
)

@Serializable
internal data class InstructorEnrollmentCreateRequestDto(
    val membershipId: Long,
)
