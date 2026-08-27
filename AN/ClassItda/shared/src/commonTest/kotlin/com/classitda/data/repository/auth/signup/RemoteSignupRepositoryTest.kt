package com.classitda.data.repository.auth.signup

import com.classitda.core.auth.InMemoryAuthTokenStorage
import com.classitda.core.network.createClassItdaHttpClient
import com.classitda.data.remote.auth.signup.SignupApi
import com.classitda.domain.model.auth.signup.GoogleIdToken
import com.classitda.domain.model.auth.signup.GoogleLoginResult
import com.classitda.domain.model.auth.signup.PhoneVerificationCode
import com.classitda.domain.model.auth.signup.SignupName
import com.classitda.domain.model.auth.signup.SignupPhoneNumber
import com.classitda.domain.model.auth.signup.SignupTermCode
import com.classitda.domain.model.auth.signup.SignupToken
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RemoteSignupRepositoryTest {
    @Test
    fun `Google 로그인 403 응답은 탈퇴 진행 상태로 변환한다`() =
        runBlocking {
            val tokenStorage = InMemoryAuthTokenStorage()
            val engine = MockEngine { respond("", HttpStatusCode.Forbidden) }
            val client =
                createClassItdaHttpClient(
                    engine = engine,
                    baseUrl = "https://api.classitda.test/",
                    tokenStorage = tokenStorage,
                )

            try {
                val result =
                    RemoteSignupRepository(SignupApi(client), tokenStorage)
                        .loginWithGoogle(GoogleIdToken("google-id-token"))

                assertEquals(GoogleLoginResult.WithdrawalPending, result)
            } finally {
                client.close()
            }
        }

    @Test
    fun `확정된 BE 계약으로 회원가입 전체 흐름을 호출한다`() =
        runBlocking {
            var requestIndex = 0
            val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
            val engine =
                MockEngine { request ->
                    assertEquals("1", request.headers["X-API-Version"])
                    when (requestIndex++) {
                        0 -> {
                            assertEquals(HttpMethod.Post, request.method)
                            assertEquals("/api/auth/google", request.url.encodedPath)
                            assertTrue(
                                request.body
                                    .toByteArray()
                                    .decodeToString()
                                    .contains("\"idToken\":\"google-id-token\""),
                            )
                            respond(
                                content =
                                    """
                                    {
                                      "status":"REGISTRATION_REQUIRED",
                                      "signupToken":"signup-token",
                                      "signupTokenExpiresIn":600
                                    }
                                    """.trimIndent(),
                                headers = jsonHeaders,
                            )
                        }

                        1 -> {
                            assertSignupAuthorization(request.headers[HttpHeaders.Authorization])
                            assertEquals(HttpMethod.Get, request.method)
                            assertEquals("/api/terms", request.url.encodedPath)
                            respond(
                                content =
                                    """
                                    [{
                                      "id":1,
                                      "code":"SERVICE_TERMS",
                                      "title":"서비스 이용약관",
                                      "url":"https://classitda.com/terms",
                                      "required":true,
                                      "version":1
                                    }]
                                    """.trimIndent(),
                                headers = jsonHeaders,
                            )
                        }

                        2 -> {
                            assertSignupAuthorization(request.headers[HttpHeaders.Authorization])
                            assertEquals("/api/auth/phone-verifications", request.url.encodedPath)
                            assertTrue(
                                request.body
                                    .toByteArray()
                                    .decodeToString()
                                    .contains("01012345678"),
                            )
                            respond(
                                content =
                                    """
                                    {
                                      "verificationId":"verification-id",
                                      "expiresInSeconds":180,
                                      "resendAfterSeconds":60
                                    }
                                    """.trimIndent(),
                                status = HttpStatusCode.Created,
                                headers = jsonHeaders,
                            )
                        }

                        3 -> {
                            assertSignupAuthorization(request.headers[HttpHeaders.Authorization])
                            assertEquals(
                                "/api/auth/phone-verifications/verification-id/confirm",
                                request.url.encodedPath,
                            )
                            assertTrue(
                                request.body
                                    .toByteArray()
                                    .decodeToString()
                                    .contains("\"otp\":\"123456\""),
                            )
                            respond(content = "", status = HttpStatusCode.NoContent)
                        }

                        4 -> {
                            assertSignupAuthorization(request.headers[HttpHeaders.Authorization])
                            assertEquals("/api/auth/signup", request.url.encodedPath)
                            val body = request.body.toByteArray().decodeToString()
                            assertTrue(body.contains("\"name\":\"클래스잇다\""))
                            assertTrue(body.contains("\"agreedTermIds\":[1]"))
                            respond(
                                content =
                                    """
                                    {
                                      "accessToken":"access-token",
                                      "accessTokenExpiresIn":3600,
                                      "refreshToken":"refresh-token",
                                      "refreshTokenExpiresIn":2592000
                                    }
                                    """.trimIndent(),
                                status = HttpStatusCode.Created,
                                headers = jsonHeaders,
                            )
                        }

                        else -> {
                            error("예상하지 못한 요청입니다: ${request.url}")
                        }
                    }
                }
            val client = createClassItdaHttpClient(engine, "https://api.classitda.test/")
            val repository = RemoteSignupRepository(SignupApi(client))

            val loginResult = repository.loginWithGoogle(GoogleIdToken("google-id-token"))
            val registrationRequired = assertIs<GoogleLoginResult.RegistrationRequired>(loginResult)
            val signupToken = registrationRequired.signupToken
            val terms = repository.getTerms(signupToken)
            val challenge = repository.requestPhoneVerification(signupToken, SignupPhoneNumber("01012345678"))
            repository.confirmPhoneVerification(
                signupToken = signupToken,
                verificationId = challenge.id,
                code = PhoneVerificationCode("123456"),
            )
            val tokens =
                repository.completeSignup(
                    signupToken = signupToken,
                    name = SignupName("클래스잇다"),
                    agreedTermIds = terms.map { it.id },
                )

            assertEquals(SignupToken("signup-token"), signupToken)
            assertEquals(SignupTermCode.SERVICE_TERMS, terms.single().code)
            assertEquals("access-token", tokens.accessToken)
            assertEquals(5, requestIndex)
            client.close()
        }

    private fun assertSignupAuthorization(value: String?) {
        assertEquals("Bearer signup-token", value)
    }
}
