package com.classitda.data.repository.instructor.mypage

import com.classitda.domain.model.instructor.mypage.FacilityRegistrationDraft
import com.classitda.domain.model.instructor.mypage.InstructorAccountProfile
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.InstructorMyPageSummary
import com.classitda.domain.model.instructor.mypage.InstructorPhoneVerificationId
import com.classitda.domain.model.instructor.mypage.ManagedFacility
import com.classitda.domain.model.instructor.mypage.ManagedMember
import com.classitda.domain.model.instructor.mypage.MemberListPage
import com.classitda.domain.model.instructor.mypage.MemberRegistrationDraft
import com.classitda.domain.model.instructor.mypage.MemberSortOrder
import com.classitda.domain.repository.instructor.mypage.FacilityList
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.domain.repository.instructor.mypage.InstructorPhoneVerificationChallenge

private typealias MemberRegistrationResult = InstructorMyPageResult<InstructorMemberId>
private typealias FacilityRegistrationResult = InstructorMyPageResult<InstructorFacilityId>

/** Deterministic app-smoke fixture; it is not an operational repository. */
internal class DemoInstructorMyPageRepository : InstructorMyPageRepository {
    private var profile =
        InstructorAccountProfile(
            id = "instructor-demo",
            name = "이지은 강사",
            phoneNumber = "01012345678",
            email = "instructor@classitda.com",
            profileImageUrl = null,
        )
    private val members =
        mutableListOf(
            ManagedMember(InstructorMemberId("member-1"), "김민지", "01012345678"),
            ManagedMember(InstructorMemberId("member-2"), "박서준", "01098765432"),
        )
    private val facilities =
        mutableListOf(
            ManagedFacility(
                id = InstructorFacilityId("facility-1"),
                name = "클래스잇다 스튜디오",
                address = "서울특별시 강남구 테헤란로",
            ),
        )

    override suspend fun getSummary() = InstructorMyPageResult.Success(InstructorMyPageSummary(profile))

    override suspend fun getProfile() = InstructorMyPageResult.Success(profile)

    override suspend fun updateProfileName(name: String) =
        InstructorMyPageResult.Success(profile.copy(name = name).also { profile = it })

    override suspend fun requestPhoneVerification(phoneNumber: String) =
        InstructorMyPageResult.Success(
            InstructorPhoneVerificationChallenge(InstructorPhoneVerificationId("verification-demo")),
        )

    override suspend fun verifyPhoneNumber(
        verificationId: InstructorPhoneVerificationId,
        phoneNumber: String,
        verificationCode: String,
    ) = InstructorMyPageResult.Success(profile.copy(phoneNumber = phoneNumber).also { profile = it })

    override suspend fun getMembers(
        query: String,
        sortOrder: MemberSortOrder,
    ): InstructorMyPageResult<MemberListPage> {
        val filtered = members.filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
        return InstructorMyPageResult.Success(MemberListPage(filtered.size, filtered))
    }

    override suspend fun registerMember(draft: MemberRegistrationDraft): MemberRegistrationResult {
        val id = InstructorMemberId("member-${members.size + 1}")
        members += ManagedMember(id, draft.name, draft.phoneNumber)
        return InstructorMyPageResult.Success(id)
    }

    override suspend fun getFacilities() =
        InstructorMyPageResult.Success(FacilityList(facilities.size, facilities.toList()))

    override suspend fun registerFacility(draft: FacilityRegistrationDraft): FacilityRegistrationResult {
        val id = InstructorFacilityId("facility-${facilities.size + 1}")
        facilities += ManagedFacility(id, draft.name, draft.address)
        return InstructorMyPageResult.Success(id)
    }
}
