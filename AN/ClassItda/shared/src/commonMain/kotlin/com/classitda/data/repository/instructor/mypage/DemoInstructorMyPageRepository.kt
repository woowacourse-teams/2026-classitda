package com.classitda.data.repository.instructor.mypage

import com.classitda.domain.model.instructor.mypage.FacilityAddress
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
import com.classitda.domain.repository.instructor.mypage.InstructorFacilityRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.domain.repository.instructor.mypage.InstructorPhoneVerificationChallenge

private typealias MemberRegistrationResult = InstructorMyPageResult<InstructorMemberId>

/** Deterministic app-smoke fixture; it is not an operational repository. */
internal class DemoInstructorMyPageRepository :
    InstructorMyPageRepository,
    InstructorFacilityRepository {
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
                address =
                    FacilityAddress(
                        roadAddress = "서울특별시 강남구 테헤란로",
                        detailAddress = "5층 501호",
                    ),
                phoneNumber = "02-1234-5678",
                description = "회원들이 편하게 운동할 수 있는 클래스잇다의 대표 시설입니다.",
                openingTime = "09:00",
                closingTime = "22:00",
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
        val sorted =
            when (sortOrder) {
                MemberSortOrder.RECENTLY_REGISTERED -> filtered
                MemberSortOrder.NAME_ASC -> filtered.sortedBy { it.name }
            }
        return InstructorMyPageResult.Success(MemberListPage(sorted.size, sorted))
    }

    override suspend fun registerMember(draft: MemberRegistrationDraft): MemberRegistrationResult {
        val id = InstructorMemberId("member-${members.size + 1}")
        members += ManagedMember(id, draft.name, draft.phoneNumber)
        return InstructorMyPageResult.Success(id)
    }

    override suspend fun getMember(memberId: InstructorMemberId) =
        members
            .firstOrNull { it.id == memberId }
            ?.let { InstructorMyPageResult.Success(it) }
            ?: InstructorMyPageResult.Failure(
                com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason.NOT_FOUND,
            )

    override suspend fun updateMember(
        memberId: InstructorMemberId,
        draft: MemberRegistrationDraft,
    ) = members
        .indexOfFirst { it.id == memberId }
        .takeIf { it >= 0 }
        ?.let { index ->
            val updated = members[index].copy(name = draft.name, phoneNumber = draft.phoneNumber)
            members[index] = updated
            InstructorMyPageResult.Success(updated)
        }
        ?: InstructorMyPageResult.Failure(
            com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason.NOT_FOUND,
        )

    override suspend fun deleteMember(memberId: InstructorMemberId) =
        if (members.removeAll { it.id == memberId }) {
            InstructorMyPageResult.Success(memberId)
        } else {
            InstructorMyPageResult.Failure(
                com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason.NOT_FOUND,
            )
        }

    override suspend fun getFacilities() =
        InstructorMyPageResult.Success(FacilityList(facilities.size, facilities.toList()))

    override suspend fun getFacility(facilityId: InstructorFacilityId) =
        facilities
            .firstOrNull { it.id == facilityId }
            ?.let { InstructorMyPageResult.Success(it) }
            ?: InstructorMyPageResult.Failure(
                com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason.NOT_FOUND,
            )

    override suspend fun registerFacility(draft: FacilityRegistrationDraft): InstructorMyPageResult<Unit> {
        val id = InstructorFacilityId("facility-${facilities.size + 1}")
        facilities += draft.toManagedFacility(id)
        return InstructorMyPageResult.Success(Unit)
    }

    override suspend fun updateFacility(
        facilityId: InstructorFacilityId,
        draft: FacilityRegistrationDraft,
    ): InstructorMyPageResult<Unit> =
        facilities
            .indexOfFirst { it.id == facilityId }
            .takeIf { it >= 0 }
            ?.let { index ->
                val updated = draft.toManagedFacility(facilityId)
                facilities[index] = updated
                InstructorMyPageResult.Success(Unit)
            }
            ?: InstructorMyPageResult.Failure(
                com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason.NOT_FOUND,
            )

    private fun FacilityRegistrationDraft.toManagedFacility(id: InstructorFacilityId) =
        ManagedFacility(
            id = id,
            name = name,
            address = address,
            image = image,
            phoneNumber = phoneNumber,
            description = description,
            openingTime = openingTime,
            closingTime = closingTime,
        )
}
