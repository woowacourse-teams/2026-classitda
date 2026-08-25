package com.classitda.domain.model.instructor.member

import com.classitda.domain.model.studio.StudioId

data class InstructorStudent(
    val id: String,
    val studioId: StudioId,
    val name: String,
    val phoneNumber: String,
    val registered: Boolean,
    val status: MembershipStatus,
)

enum class MembershipStatus {
    ACTIVE,
    INACTIVE,
    WITHDRAWN,
}

data class InstructorStudentPage(
    val items: List<InstructorStudent>,
    val hasNext: Boolean,
    val nextCursor: String?,
)
