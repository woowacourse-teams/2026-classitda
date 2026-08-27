package com.classitda.domain.repository.instructor.mypage

import com.classitda.domain.model.instructor.mypage.InstructorAccountProfile
import com.classitda.domain.model.instructor.mypage.InstructorPhoneVerificationId

interface InstructorMyPageRepository {
    suspend fun requestPhoneVerification(
        phoneNumber: String,
    ): InstructorMyPageResult<InstructorPhoneVerificationChallenge>

    suspend fun verifyPhoneNumber(
        verificationId: InstructorPhoneVerificationId,
        phoneNumber: String,
        verificationCode: String,
    ): InstructorMyPageResult<InstructorAccountProfile>
}
