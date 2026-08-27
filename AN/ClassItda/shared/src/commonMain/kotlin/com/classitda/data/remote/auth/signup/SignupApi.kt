package com.classitda.data.remote.auth.signup

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class SignupApi(
    private val client: HttpClient,
) {
    suspend fun loginWithGoogle(request: GoogleLoginRequestDto): LoginResponseDto =
        client
            .post("api/auth/google") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

    suspend fun getTerms(signupToken: String): List<SignupTermResponseDto> =
        client.get("api/terms") { bearerAuth(signupToken) }.body()

    suspend fun requestPhoneVerification(
        signupToken: String,
        request: PhoneVerificationSendRequestDto,
    ): PhoneVerificationResponseDto =
        client
            .post("api/auth/phone-verifications") {
                bearerAuth(signupToken)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

    suspend fun confirmPhoneVerification(
        signupToken: String,
        verificationId: String,
        request: PhoneVerificationConfirmRequestDto,
    ) {
        client.post("api/auth/phone-verifications/$verificationId/confirm") {
            bearerAuth(signupToken)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun completeSignup(
        signupToken: String,
        request: SignupRequestDto,
    ): SignupResponseDto =
        client
            .post("api/auth/signup") {
                bearerAuth(signupToken)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

    suspend fun logout(
        accessToken: String,
        request: LogoutRequestDto,
    ) {
        client.post("api/auth/logout") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
