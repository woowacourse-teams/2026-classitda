package com.classitda.domain.repository.instructor.membership

import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.ManagedMember
import com.classitda.domain.model.instructor.mypage.MemberListPage
import com.classitda.domain.model.instructor.mypage.MemberRegistrationDraft
import com.classitda.domain.model.studio.StudioId
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult

interface InstructorMembershipRepository {
    suspend fun getStudents(
        studioId: StudioId,
        cursor: String?,
        size: Int,
    ): InstructorMyPageResult<MemberListPage>

    suspend fun registerStudent(
        studioId: StudioId,
        draft: MemberRegistrationDraft,
    ): InstructorMyPageResult<Unit>

    suspend fun getMembership(
        studioId: StudioId,
        membershipId: InstructorMemberId,
    ): InstructorMyPageResult<ManagedMember>

    suspend fun updateStudent(
        studioId: StudioId,
        membershipId: InstructorMemberId,
        draft: MemberRegistrationDraft,
    ): InstructorMyPageResult<Unit>

    suspend fun deleteMembership(
        studioId: StudioId,
        membershipId: InstructorMemberId,
    ): InstructorMyPageResult<Unit>
}
