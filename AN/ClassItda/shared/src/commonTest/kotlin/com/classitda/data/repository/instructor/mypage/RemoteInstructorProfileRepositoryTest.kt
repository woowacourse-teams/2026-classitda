package com.classitda.data.repository.instructor.mypage

import com.classitda.core.auth.InMemoryAuthTokenStorage
import com.classitda.core.network.createClassItdaHttpClient
import com.classitda.data.remote.member.MemberApi
import com.classitda.domain.model.auth.signup.LoginTokens
import com.classitda.domain.model.instructor.mypage.InstructorAccountProfile
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RemoteInstructorProfileRepositoryTest {
    @Test
    fun `이름 수정 성공 후 최신 내 정보로 profile을 갱신한다`() =
        runBlocking {
            var requestIndex = 0
            val engine =
                MockEngine { request ->
                    assertEquals("1", request.headers["X-API-Version"])
                    assertEquals("Bearer access-token", request.headers[HttpHeaders.Authorization])
                    when (requestIndex++) {
                        0 -> {
                            assertEquals(HttpMethod.Patch, request.method)
                            assertEquals("/api/members/me/name", request.url.encodedPath)
                            respond("", status = HttpStatusCode.NoContent)
                        }

                        1 -> {
                            assertEquals(HttpMethod.Get, request.method)
                            assertEquals("/api/members/me", request.url.encodedPath)
                            respond(
                                """
                                {"name":"변경 이름","phoneNumber":"01098765432","email":"instructor@classitda.com"}
                                """.trimIndent(),
                                headers = jsonHeaders,
                            )
                        }

                        else -> error("예상하지 못한 요청입니다: ${request.url}")
                    }
                }
            val client =
                createClassItdaHttpClient(
                    engine,
                    BASE_URL,
                    InMemoryAuthTokenStorage().apply { write(testTokens) },
                )

            try {
                val result = RemoteInstructorProfileRepository(MemberApi(client)).updateProfileName("변경 이름")

                val profile = assertIs<InstructorMyPageResult.Success<InstructorAccountProfile>>(result).value
                assertEquals("변경 이름", profile.name)
                assertEquals("01098765432", profile.phoneNumber)
                assertEquals("instructor@classitda.com", profile.email)
                assertEquals(2, requestIndex)
            } finally {
                client.close()
            }
        }

    @Test
    fun `내 정보 조회의 404는 NOT_FOUND로 변환한다`() =
        runBlocking {
            val engine =
                MockEngine {
                    respond("", status = HttpStatusCode.NotFound)
                }
            val client =
                createClassItdaHttpClient(
                    engine,
                    BASE_URL,
                    InMemoryAuthTokenStorage().apply { write(testTokens) },
                )

            try {
                val result = RemoteInstructorProfileRepository(MemberApi(client)).getProfile()

                assertEquals(
                    InstructorMyPageFailureReason.NOT_FOUND,
                    assertIs<InstructorMyPageResult.Failure>(result).reason,
                )
            } finally {
                client.close()
            }
        }

    private companion object {
        const val BASE_URL = "https://api.classitda.test/"
        val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
        val testTokens =
            LoginTokens(
                accessToken = "access-token",
                accessTokenExpiresInSeconds = 3600,
                refreshToken = "refresh-token",
                refreshTokenExpiresInSeconds = 86400,
            )
    }
}
