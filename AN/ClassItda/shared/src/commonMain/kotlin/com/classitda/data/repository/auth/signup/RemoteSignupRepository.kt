package com.classitda.data.repository.auth.signup

import com.classitda.data.remote.auth.signup.GoogleLoginRequestDto
import com.classitda.data.remote.auth.signup.LoginResponseDto
import com.classitda.data.remote.auth.signup.LoginStatusDto
import com.classitda.data.remote.auth.signup.PhoneVerificationConfirmRequestDto
import com.classitda.data.remote.auth.signup.PhoneVerificationResponseDto
import com.classitda.data.remote.auth.signup.PhoneVerificationSendRequestDto
import com.classitda.data.remote.auth.signup.SignupApi
import com.classitda.data.remote.auth.signup.SignupRequestDto
import com.classitda.data.remote.auth.signup.SignupResponseDto
import com.classitda.data.remote.auth.signup.SignupTermCodeDto
import com.classitda.data.remote.auth.signup.SignupTermResponseDto
import com.classitda.domain.model.auth.signup.GoogleIdToken
import com.classitda.domain.model.auth.signup.GoogleLoginResult
import com.classitda.domain.model.auth.signup.LoginTokens
import com.classitda.domain.model.auth.signup.PhoneVerificationChallenge
import com.classitda.domain.model.auth.signup.PhoneVerificationCode
import com.classitda.domain.model.auth.signup.PhoneVerificationId
import com.classitda.domain.model.auth.signup.SignupName
import com.classitda.domain.model.auth.signup.SignupPhoneNumber
import com.classitda.domain.model.auth.signup.SignupTerm
import com.classitda.domain.model.auth.signup.SignupTermCode
import com.classitda.domain.model.auth.signup.SignupToken
import com.classitda.domain.model.auth.signup.TermId
import com.classitda.domain.repository.auth.signup.SignupRepository

internal class RemoteSignupRepository(
    private val api: SignupApi,
) : SignupRepository {
    override suspend fun loginWithGoogle(idToken: GoogleIdToken): GoogleLoginResult =
        api.loginWithGoogle(GoogleLoginRequestDto(idToken.value)).toDomain()

    override suspend fun getTerms(signupToken: SignupToken): List<SignupTerm> =
        api.getTerms(signupToken.value).map(SignupTermResponseDto::toDomain)

    override suspend fun requestPhoneVerification(
        signupToken: SignupToken,
        phoneNumber: SignupPhoneNumber,
    ): PhoneVerificationChallenge =
        api.requestPhoneVerification(signupToken.value, PhoneVerificationSendRequestDto(phoneNumber.value)).toDomain()

    override suspend fun confirmPhoneVerification(
        signupToken: SignupToken,
        verificationId: PhoneVerificationId,
        code: PhoneVerificationCode,
    ) {
        api.confirmPhoneVerification(
            signupToken.value,
            verificationId.value,
            PhoneVerificationConfirmRequestDto(code.value),
        )
    }

    override suspend fun completeSignup(
        signupToken: SignupToken,
        name: SignupName,
        agreedTermIds: List<TermId>,
    ): LoginTokens {
        require(agreedTermIds.isNotEmpty()) { "동의한 약관은 한 개 이상이어야 합니다." }
        return api
            .completeSignup(
                signupToken.value,
                SignupRequestDto(name.value, agreedTermIds.map(TermId::value)),
            ).toDomain()
    }
}

private fun LoginResponseDto.toDomain(): GoogleLoginResult =
    when (status) {
        LoginStatusDto.REGISTERED -> {
            GoogleLoginResult.Registered(toLoginTokens())
        }

        LoginStatusDto.REGISTRATION_REQUIRED -> {
            GoogleLoginResult.RegistrationRequired(
                signupToken = SignupToken(requireNotNull(signupToken)),
                expiresInSeconds = requireNotNull(signupTokenExpiresIn),
            )
        }
    }

private fun LoginResponseDto.toLoginTokens() =
    LoginTokens(
        accessToken = requireNotNull(accessToken),
        accessTokenExpiresInSeconds = requireNotNull(accessTokenExpiresIn),
        refreshToken = requireNotNull(refreshToken),
        refreshTokenExpiresInSeconds = requireNotNull(refreshTokenExpiresIn),
    )

private fun SignupTermResponseDto.toDomain() =
    SignupTerm(
        id = TermId(id),
        code = code.toDomain(),
        title = title,
        url = url,
        required = required,
        version = version,
    )

private fun SignupTermCodeDto.toDomain() = SignupTermCode.valueOf(name)

private fun PhoneVerificationResponseDto.toDomain() =
    PhoneVerificationChallenge(
        id = PhoneVerificationId(verificationId),
        expiresInSeconds = expiresInSeconds,
        resendAfterSeconds = resendAfterSeconds,
    )

private fun SignupResponseDto.toDomain() =
    LoginTokens(
        accessToken = accessToken,
        accessTokenExpiresInSeconds = accessTokenExpiresIn,
        refreshToken = refreshToken,
        refreshTokenExpiresInSeconds = refreshTokenExpiresIn,
    )
