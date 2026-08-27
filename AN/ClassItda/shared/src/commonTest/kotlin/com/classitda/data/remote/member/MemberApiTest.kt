package com.classitda.data.remote.member

import com.classitda.core.auth.InMemoryAuthTokenStorage
import com.classitda.core.network.createClassItdaHttpClient
import com.classitda.domain.model.auth.signup.LoginTokens
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MemberApiTest {
    @Test
    fun `내 정보 조회는 Swagger 응답 필드를 역직렬화한다`() =
        runBlocking {
            val engine =
                MockEngine { request ->
                    assertEquals(HttpMethod.Get, request.method)
                    assertEquals("/api/members/me", request.url.encodedPath)
                    assertEquals("1", request.headers["X-API-Version"])
                    assertEquals("Bearer access-token", request.headers[HttpHeaders.Authorization])
                    respond(
                        """
                        {"name":"이지은","phoneNumber":"01012345678","email":null}
                        """.trimIndent(),
                        headers = jsonHeaders,
                    )
                }
            val client = createClient(engine)

            try {
                val profile = MemberApi(client).getMe()

                assertEquals("이지은", profile.name)
                assertEquals("01012345678", profile.phoneNumber)
                assertNull(profile.email)
            } finally {
                client.close()
            }
        }

    @Test
    fun `내 이름 수정은 JSON body와 204 응답을 사용한다`() =
        runBlocking {
            val engine =
                MockEngine { request ->
                    assertEquals(HttpMethod.Patch, request.method)
                    assertEquals("/api/members/me/name", request.url.encodedPath)
                    assertEquals("1", request.headers["X-API-Version"])
                    assertEquals("Bearer access-token", request.headers[HttpHeaders.Authorization])
                    val body = assertIs<OutgoingContent>(request.body)
                    assertEquals(ContentType.Application.Json, body.contentType)
                    val json = request.body.toByteArray().decodeToString()
                    assertTrue(json.contains("\"name\":\"새 이름\""))
                    respond("", status = HttpStatusCode.NoContent)
                }
            val client = createClient(engine)

            try {
                MemberApi(client).updateName("새 이름")
            } finally {
                client.close()
            }
        }

    private fun createClient(engine: MockEngine) =
        createClassItdaHttpClient(
            engine = engine,
            baseUrl = BASE_URL,
            tokenStorage = InMemoryAuthTokenStorage().apply { write(testTokens) },
        )

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
