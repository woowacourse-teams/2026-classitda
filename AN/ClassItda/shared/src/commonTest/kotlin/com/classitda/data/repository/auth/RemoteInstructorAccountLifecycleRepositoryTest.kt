package com.classitda.data.repository.auth

import com.classitda.core.auth.InMemoryAuthTokenStorage
import com.classitda.core.auth.SessionCacheCleaner
import com.classitda.core.network.createClassItdaHttpClient
import com.classitda.data.remote.member.MemberApi
import com.classitda.domain.model.auth.signup.LoginTokens
import com.classitda.domain.repository.auth.AccountLifecycleFailureReason
import com.classitda.domain.repository.auth.AccountLifecycleResult
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RemoteInstructorAccountLifecycleRepositoryTest {
    @Test
    fun `회원 탈퇴는 DELETE API의 204 응답을 성공으로 처리한다`() =
        runBlocking {
            val engine =
                MockEngine { request ->
                    assertEquals(HttpMethod.Delete, request.method)
                    assertEquals("/api/members/me", request.url.encodedPath)
                    assertEquals("1", request.headers["X-API-Version"])
                    assertEquals("Bearer access-token", request.headers[HttpHeaders.Authorization])
                    respond("", HttpStatusCode.NoContent)
                }
            val client = createClient(engine)

            try {
                val result = RemoteInstructorAccountLifecycleRepository(MemberApi(client)).withdraw()

                assertEquals(AccountLifecycleResult.Success, result)
            } finally {
                client.close()
            }
        }

    @Test
    fun `시설 대표 탈퇴 불가 409 응답을 충돌 오류로 처리한다`() =
        runBlocking {
            val engine = MockEngine { respond("", HttpStatusCode.Conflict) }
            val client = createClient(engine)

            try {
                val result = RemoteInstructorAccountLifecycleRepository(MemberApi(client)).withdraw()

                val failure = assertIs<AccountLifecycleResult.Failure>(result)
                assertEquals(AccountLifecycleFailureReason.CONFLICT, failure.reason)
            } finally {
                client.close()
            }
        }

    @Test
    fun `캐시 삭제에 실패하면 탈퇴 API를 호출하지 않고 성공을 반환하지 않는다`() =
        runBlocking {
            var requestCount = 0
            val engine =
                MockEngine {
                    requestCount++
                    respond("", HttpStatusCode.NoContent)
                }
            val client = createClient(engine)

            try {
                val result =
                    RemoteInstructorAccountLifecycleRepository(
                        MemberApi(client),
                        SessionCacheCleaner { error("cache clear failed") },
                    ).withdraw()

                val failure = assertIs<AccountLifecycleResult.Failure>(result)
                assertEquals(AccountLifecycleFailureReason.UNKNOWN, failure.reason)
                assertEquals(0, requestCount)
            } finally {
                client.close()
            }
        }

    private fun createClient(engine: MockEngine) =
        createClassItdaHttpClient(
            engine = engine,
            baseUrl = "https://api.classitda.test/",
            tokenStorage =
                InMemoryAuthTokenStorage().apply {
                    write(
                        LoginTokens(
                            accessToken = "access-token",
                            accessTokenExpiresInSeconds = 3600,
                            refreshToken = "refresh-token",
                            refreshTokenExpiresInSeconds = 86400,
                        ),
                    )
                },
        )
}
