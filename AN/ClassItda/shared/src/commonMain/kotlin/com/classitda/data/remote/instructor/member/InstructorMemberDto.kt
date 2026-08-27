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
internal data class InstructorEnrollmentCreateRequestDto(
    val membershipId: Long,
)
