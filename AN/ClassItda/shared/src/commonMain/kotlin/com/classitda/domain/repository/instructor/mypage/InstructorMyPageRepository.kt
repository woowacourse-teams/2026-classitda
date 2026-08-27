package com.classitda.domain.repository.instructor.mypage

import com.classitda.domain.model.instructor.mypage.InstructorAccountProfile
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.InstructorMyPageSummary
import com.classitda.domain.model.instructor.mypage.InstructorPhoneVerificationId
import com.classitda.domain.model.instructor.mypage.ManagedMember
import com.classitda.domain.model.instructor.mypage.MemberListPage
import com.classitda.domain.model.instructor.mypage.MemberRegistrationDraft
import com.classitda.domain.model.instructor.mypage.MemberSortOrder

interface InstructorMyPageRepository {
    suspend fun getSummary(): InstructorMyPageResult<InstructorMyPageSummary>

    suspend fun getProfile(): InstructorMyPageResult<InstructorAccountProfile>

    suspend fun updateProfileName(name: String): InstructorMyPageResult<InstructorAccountProfile>

    suspend fun requestPhoneVerification(
        phoneNumber: String,
    ): InstructorMyPageResult<InstructorPhoneVerificationChallenge>

    suspend fun verifyPhoneNumber(
        verificationId: InstructorPhoneVerificationId,
        phoneNumber: String,
        verificationCode: String,
    ): InstructorMyPageResult<InstructorAccountProfile>

    /** A blank query retrieves the full member list; a non-blank query searches it. */
    suspend fun getMembers(
        query: String = "",
        sortOrder: MemberSortOrder = MemberSortOrder.RECENTLY_REGISTERED,
    ): InstructorMyPageResult<MemberListPage>

    suspend fun registerMember(draft: MemberRegistrationDraft): InstructorMyPageResult<InstructorMemberId>

    suspend fun getMember(memberId: InstructorMemberId): InstructorMyPageResult<ManagedMember>

    suspend fun updateMember(
        memberId: InstructorMemberId,
        draft: MemberRegistrationDraft,
    ): InstructorMyPageResult<ManagedMember>

    suspend fun deleteMember(memberId: InstructorMemberId): InstructorMyPageResult<InstructorMemberId>
}
