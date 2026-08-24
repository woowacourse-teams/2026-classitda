package com.classitda.domain.repository.auth.signup

import com.classitda.domain.model.auth.signup.GoogleIdToken
import com.classitda.domain.model.auth.signup.GoogleLoginResult
import com.classitda.domain.model.auth.signup.LoginTokens
import com.classitda.domain.model.auth.signup.PhoneVerificationChallenge
import com.classitda.domain.model.auth.signup.PhoneVerificationCode
import com.classitda.domain.model.auth.signup.PhoneVerificationId
import com.classitda.domain.model.auth.signup.SignupName
import com.classitda.domain.model.auth.signup.SignupPhoneNumber
import com.classitda.domain.model.auth.signup.SignupTerm
import com.classitda.domain.model.auth.signup.SignupToken
import com.classitda.domain.model.auth.signup.TermId

interface SignupRepository {
    suspend fun loginWithGoogle(idToken: GoogleIdToken): GoogleLoginResult

    suspend fun getTerms(signupToken: SignupToken): List<SignupTerm>

    suspend fun requestPhoneVerification(
        signupToken: SignupToken,
        phoneNumber: SignupPhoneNumber,
    ): PhoneVerificationChallenge

    suspend fun confirmPhoneVerification(
        signupToken: SignupToken,
        verificationId: PhoneVerificationId,
        code: PhoneVerificationCode,
    )

    suspend fun completeSignup(
        signupToken: SignupToken,
        name: SignupName,
        agreedTermIds: List<TermId>,
    ): LoginTokens

    suspend fun logout()
}
