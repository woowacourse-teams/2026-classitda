package com.classitda.data.repository.instructor.mypage

import com.classitda.domain.model.instructor.mypage.InstructorAccountProfile
import com.classitda.domain.model.instructor.mypage.InstructorPhoneVerificationId
import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.domain.model.instructor.mypage.ManagedStudio
import com.classitda.domain.model.instructor.mypage.StudioAddress
import com.classitda.domain.model.instructor.mypage.StudioImageMutation
import com.classitda.domain.model.instructor.mypage.StudioRegistrationDraft
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.domain.repository.instructor.mypage.InstructorPhoneVerificationChallenge
import com.classitda.domain.repository.instructor.mypage.InstructorStudioRepository
import com.classitda.domain.repository.instructor.mypage.StudioList

/** Deterministic app-smoke fixture; it is not an operational repository. */
internal class DemoInstructorMyPageRepository :
    InstructorMyPageRepository,
    InstructorStudioRepository {
    private var profile =
        InstructorAccountProfile(
            name = "이지은 강사",
            phoneNumber = "01012345678",
            email = "instructor@classitda.com",
            profileImageUrl = null,
        )
    private val studios =
        mutableListOf(
            ManagedStudio(
                id = InstructorStudioId("studio-1"),
                name = "클래스잇다 스튜디오",
                address =
                    StudioAddress(
                        roadAddress = "서울특별시 강남구 테헤란로",
                        detailAddress = "5층 501호",
                    ),
                phoneNumber = "02-1234-5678",
                description = "회원들이 편하게 운동할 수 있는 클래스잇다의 대표 시설입니다.",
                openingTime = "09:00",
                closingTime = "22:00",
            ),
        )

    override suspend fun requestPhoneVerification(phoneNumber: String) =
        InstructorMyPageResult.Success(
            InstructorPhoneVerificationChallenge(InstructorPhoneVerificationId("verification-demo")),
        )

    override suspend fun verifyPhoneNumber(
        verificationId: InstructorPhoneVerificationId,
        phoneNumber: String,
        verificationCode: String,
    ) = InstructorMyPageResult.Success(profile.copy(phoneNumber = phoneNumber).also { profile = it })

    override suspend fun getStudios() = InstructorMyPageResult.Success(StudioList(studios.size, studios.toList()))

    override suspend fun getStudio(studioId: InstructorStudioId) =
        studios
            .firstOrNull { it.id == studioId }
            ?.let { InstructorMyPageResult.Success(it) }
            ?: InstructorMyPageResult.Failure(
                com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason.NOT_FOUND,
            )

    override suspend fun registerStudio(draft: StudioRegistrationDraft): InstructorMyPageResult<Unit> {
        val id = InstructorStudioId("studio-${studios.size + 1}")
        studios += draft.toManagedStudio(id)
        return InstructorMyPageResult.Success(Unit)
    }

    override suspend fun updateStudio(
        studioId: InstructorStudioId,
        original: ManagedStudio,
        draft: StudioRegistrationDraft,
        imageMutation: StudioImageMutation,
    ): InstructorMyPageResult<Unit> =
        studios
            .indexOfFirst { it.id == studioId }
            .takeIf { it >= 0 }
            ?.let { index ->
                val updated = draft.toManagedStudio(studioId)
                studios[index] = updated
                InstructorMyPageResult.Success(Unit)
            }
            ?: InstructorMyPageResult.Failure(
                com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason.NOT_FOUND,
            )

    private fun StudioRegistrationDraft.toManagedStudio(id: InstructorStudioId) =
        ManagedStudio(
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
