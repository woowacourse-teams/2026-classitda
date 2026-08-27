package com.classitda.domain.repository.instructor.mypage

import com.classitda.domain.model.instructor.mypage.InstructorPhoneVerificationId
import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.domain.model.instructor.mypage.ManagedStudio
import com.classitda.domain.model.instructor.mypage.StudioAddress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class InstructorMyPageRepositoryTest {
    @Test
    fun resultKeepsSuccessValueAndFailureReason() {
        val success = InstructorMyPageResult.Success("summary")
        val failure = InstructorMyPageResult.Failure(InstructorMyPageFailureReason.NETWORK)

        assertEquals("summary", assertIs<InstructorMyPageResult.Success<String>>(success).value)
        assertEquals(InstructorMyPageFailureReason.NETWORK, failure.reason)
    }

    @Test
    fun phoneVerificationChallengeKeepsTypedId() {
        val id = InstructorPhoneVerificationId("verification-1")

        assertEquals(id, InstructorPhoneVerificationChallenge(id).verificationId)
    }

    @Test
    fun studioListKeepsRepositoryTotalCountSeparatelyFromItems() {
        val studio =
            ManagedStudio(
                id = InstructorStudioId("studio-1"),
                name = "Studio",
                address = StudioAddress(roadAddress = "Seoul"),
            )
        val page = StudioList(totalCount = 12, studios = listOf(studio))

        assertEquals(12, page.totalCount)
        assertEquals(listOf(studio), page.studios)
    }
}
