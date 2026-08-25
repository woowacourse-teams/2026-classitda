package com.classitda.domain.repository.instructor.member

import com.classitda.domain.model.instructor.member.InstructorStudentPage
import com.classitda.domain.model.studio.StudioId

interface InstructorMemberRepository {
    suspend fun getStudents(
        studioId: StudioId,
        cursor: String? = null,
        size: Int = DEFAULT_PAGE_SIZE,
    ): InstructorStudentPage

    suspend fun enrollStudent(
        studioId: StudioId,
        sessionId: String,
        membershipId: String,
    )

    suspend fun cancelEnrollment(
        studioId: StudioId,
        sessionId: String,
        enrollmentId: String,
    )

    private companion object {
        const val DEFAULT_PAGE_SIZE = 100
    }
}
