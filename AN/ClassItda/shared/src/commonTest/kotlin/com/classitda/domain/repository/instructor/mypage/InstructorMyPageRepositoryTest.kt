package com.classitda.domain.repository.instructor.mypage

import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.InstructorPhoneVerificationId
import com.classitda.domain.model.instructor.mypage.ManagedFacility
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
    fun facilityListKeepsRepositoryTotalCountSeparatelyFromItems() {
        val facility =
            ManagedFacility(
                id = InstructorFacilityId("facility-1"),
                name = "Studio",
                address = "Seoul",
            )
        val page = FacilityList(totalCount = 12, facilities = listOf(facility))

        assertEquals(12, page.totalCount)
        assertEquals(listOf(facility), page.facilities)
    }
}
