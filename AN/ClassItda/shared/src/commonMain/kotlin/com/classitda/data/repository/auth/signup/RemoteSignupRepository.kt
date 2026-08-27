package com.classitda.data.repository.auth.signup

import co.touchlab.kermit.Logger
import com.classitda.core.auth.AuthTokenStorage
import com.classitda.core.auth.InMemoryAuthTokenStorage
import com.classitda.core.auth.SessionCacheCleaner
import com.classitda.data.remote.auth.signup.GoogleLoginRequestDto
import com.classitda.data.remote.auth.signup.LoginResponseDto
import com.classitda.data.remote.auth.signup.LoginStatusDto
import com.classitda.data.remote.auth.signup.LogoutRequestDto
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
import io.ktor.client.plugins.ResponseException

internal class RemoteSignupRepository(
    private val api: SignupApi,
    private val tokenStorage: AuthTokenStorage = InMemoryAuthTokenStorage(),
    private val sessionCacheCleaner: SessionCacheCleaner = SessionCacheCleaner { },
) : SignupRepository {
    override suspend fun loginWithGoogle(idToken: GoogleIdToken): GoogleLoginResult =
        try {
            val response = api.loginWithGoogle(GoogleLoginRequestDto(idToken.value))
            Logger.d("SignupFlow: Google login API response status=${response.status}")
            response.toDomain().also { result ->
                if (result is GoogleLoginResult.Registered) {
                    clearSessionCacheBestEffort()
                    tokenStorage.write(result.tokens)
                }
            }
        } catch (exception: ResponseException) {
            if (exception.response.status.value == 403) {
                Logger.d("SignupFlow: Google login returned 403; treating account as withdrawal pending")
                GoogleLoginResult.WithdrawalPending
            } else {
                throw exception
            }
        }

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
        val tokens =
            api
                .completeSignup(
                    signupToken.value,
                    SignupRequestDto(name.value, agreedTermIds.map(TermId::value)),
                ).toDomain()
        clearSessionCacheBestEffort()
        tokenStorage.write(tokens)
        return tokens
    }

    override suspend fun logout() {
        val tokens =
            tokenStorage.read()
                ?: run {
                    Logger.d("AuthSession: logout skipped because no local token exists")
                    clearSessionCacheBestEffort()
                    return
                }
        Logger.d("AuthSession: logout API call with local tokens")
        runCatching {
            api.logout(tokens.accessToken, LogoutRequestDto(tokens.refreshToken))
        }.onSuccess {
            Logger.d("AuthSession: logout API succeeded")
        }.onFailure { error ->
            Logger.e("AuthSession: logout API failed: ${error.message}")
        }.also {
            runCatching { sessionCacheCleaner.clear() }
                .onFailure { error -> Logger.e("AuthSession: local cache clear failed: ${error.message}") }
            tokenStorage.clear()
            Logger.d("AuthSession: local tokens cleared")
        }
    }

    private suspend fun clearSessionCacheBestEffort() {
        runCatching { sessionCacheCleaner.clear() }
            .onFailure { error -> Logger.e("AuthSession: local cache clear failed: ${error.message}") }
    }
}

private fun LoginResponseDto.toDomain(): GoogleLoginResult =
    when (status) {
        LoginStatusDto.REGISTERED.name -> {
            GoogleLoginResult.Registered(toLoginTokens())
        }

        LoginStatusDto.REGISTRATION_REQUIRED.name -> {
            GoogleLoginResult.RegistrationRequired(
                signupToken = SignupToken(requireNotNull(signupToken)),
                expiresInSeconds = requireNotNull(signupTokenExpiresIn),
            )
        }

        else -> {
            error("지원하지 않는 Google 로그인 상태: $status")
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
