package com.classitda.data.repository.instructor.member

import com.classitda.data.remote.instructor.member.InstructorMemberApi
import com.classitda.data.remote.instructor.member.StudioMembershipResponseDto
import com.classitda.domain.model.instructor.member.InstructorStudent
import com.classitda.domain.model.instructor.member.InstructorStudentPage
import com.classitda.domain.model.instructor.member.MembershipStatus
import com.classitda.domain.model.studio.StudioId
import com.classitda.domain.repository.instructor.member.InstructorMemberRepository

internal class RemoteInstructorMemberRepository(
    private val api: InstructorMemberApi,
) : InstructorMemberRepository {
    override suspend fun getStudents(
        studioId: StudioId,
        cursor: String?,
        size: Int,
    ): InstructorStudentPage =
        api.getStudents(studioId.value, cursor, size).let { response ->
            InstructorStudentPage(
                items = response.items.map { it.toDomain(studioId) },
                hasNext = response.hasNext,
                nextCursor = response.nextCursor,
            )
        }

    override suspend fun enrollStudent(
        studioId: StudioId,
        sessionId: String,
        membershipId: String,
    ) {
        api.enrollStudent(studioId.value, sessionId, membershipId.toLong())
    }

    override suspend fun cancelEnrollment(
        studioId: StudioId,
        sessionId: String,
        enrollmentId: String,
    ) {
        api.cancelEnrollment(studioId.value, sessionId, enrollmentId)
    }
}

private fun StudioMembershipResponseDto.toDomain(studioId: StudioId) =
    InstructorStudent(
        id = id.toString(),
        studioId = studioId,
        name = name,
        phoneNumber = phoneNumber,
        registered = registered,
        status = status.toMembershipStatus(),
    )

private fun String.toMembershipStatus() =
    when (this) {
        "ACTIVE" -> MembershipStatus.ACTIVE
        "INACTIVE" -> MembershipStatus.INACTIVE
        "WITHDRAWN" -> MembershipStatus.WITHDRAWN
        else -> error("지원하지 않는 회원 소속 상태입니다: $this")
    }
