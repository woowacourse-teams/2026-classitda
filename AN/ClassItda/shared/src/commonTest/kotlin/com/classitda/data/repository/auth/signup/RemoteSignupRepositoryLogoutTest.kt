package com.classitda.data.repository.auth.signup

import com.classitda.core.auth.InMemoryAuthTokenStorage
import com.classitda.core.auth.SessionCacheCleaner
import com.classitda.core.network.createClassItdaHttpClient
import com.classitda.data.remote.auth.signup.SignupApi
import com.classitda.domain.model.auth.signup.LoginTokens
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RemoteSignupRepositoryLogoutTest {
    @Test
    fun `로그아웃은 현재 기기의 refresh token을 전송하고 로컬 토큰을 삭제한다`() =
        runBlocking {
            val tokenStorage =
                InMemoryAuthTokenStorage().apply {
                    write(
                        LoginTokens(
                            accessToken = "access-token",
                            accessTokenExpiresInSeconds = 3600,
                            refreshToken = "refresh-token",
                            refreshTokenExpiresInSeconds = 2592000,
                        ),
                    )
                }
            val engine =
                MockEngine { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    assertEquals("/api/auth/logout", request.url.encodedPath)
                    assertEquals("1", request.headers["X-API-Version"])
                    assertEquals("Bearer access-token", request.headers[HttpHeaders.Authorization])
                    assertTrue(
                        request.body
                            .toByteArray()
                            .decodeToString()
                            .contains("\"refreshToken\":\"refresh-token\""),
                    )
                    respond("", HttpStatusCode.NoContent)
                }
            val client =
                createClassItdaHttpClient(
                    engine = engine,
                    baseUrl = "https://api.classitda.test/",
                    tokenStorage = tokenStorage,
                )

            try {
                RemoteSignupRepository(SignupApi(client), tokenStorage).logout()
            } finally {
                client.close()
            }

            assertNull(tokenStorage.read())
            Unit
        }

    @Test
    fun `로그아웃 API가 실패해도 로컬 토큰을 삭제한다`() =
        runBlocking {
            val tokenStorage =
                InMemoryAuthTokenStorage().apply {
                    write(
                        LoginTokens(
                            accessToken = "access-token",
                            accessTokenExpiresInSeconds = 3600,
                            refreshToken = "refresh-token",
                            refreshTokenExpiresInSeconds = 2592000,
                        ),
                    )
                }
            val engine =
                MockEngine {
                    respond("", HttpStatusCode.InternalServerError)
                }
            val client =
                createClassItdaHttpClient(
                    engine = engine,
                    baseUrl = "https://api.classitda.test/",
                    tokenStorage = tokenStorage,
                )

            try {
                RemoteSignupRepository(SignupApi(client), tokenStorage).logout()
            } finally {
                client.close()
            }

            assertNull(tokenStorage.read())
            Unit
        }

    @Test
    fun `캐시 삭제에 실패하면 로그아웃 토큰을 삭제하지 않고 예외를 전달한다`() =
        runBlocking {
            val tokenStorage =
                InMemoryAuthTokenStorage().apply {
                    write(
                        LoginTokens(
                            accessToken = "access-token",
                            accessTokenExpiresInSeconds = 3600,
                            refreshToken = "refresh-token",
                            refreshTokenExpiresInSeconds = 2592000,
                        ),
                    )
                }
            val client =
                createClassItdaHttpClient(
                    engine = MockEngine { respond("", HttpStatusCode.NoContent) },
                    baseUrl = "https://api.classitda.test/",
                    tokenStorage = tokenStorage,
                )

            try {
                val repository =
                    RemoteSignupRepository(
                        SignupApi(client),
                        tokenStorage,
                        SessionCacheCleaner { error("cache clear failed") },
                    )

                assertFailsWith<IllegalStateException> { repository.logout() }
                assertEquals("access-token", tokenStorage.read()?.accessToken)
            } finally {
                client.close()
            }
        }
}
