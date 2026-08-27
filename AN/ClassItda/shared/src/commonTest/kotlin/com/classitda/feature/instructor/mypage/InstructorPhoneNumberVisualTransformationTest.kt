package com.classitda.feature.instructor.mypage

import kotlin.test.Test
import kotlin.test.assertEquals

class InstructorPhoneNumberVisualTransformationTest {
    @Test
    fun formatsMobilePhoneNumberLikeStudioRegistration() {
        assertEquals("010-1234-5678", formatInstructorPhoneNumber("01012345678"))
    }

    @Test
    fun formatsSeoulPhoneNumberLikeStudioRegistration() {
        assertEquals("02-1234-5678", formatInstructorPhoneNumber("02-12345678"))
    }

    @Test
    fun keepsOnlyElevenPhoneDigits() {
        assertEquals("01012345678", instructorPhoneNumberDigits("010-1234-5678-999"))
    }
}
