package com.classitda.data.remote.instructor.member

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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InstructorMemberApiTest {
    @Test
    fun `회원 목록은 첫 요청에서 cursor를 생략하고 10개 단위 커서를 전달한다`() =
        runBlocking {
            var requestIndex = 0
            val engine =
                MockEngine { request ->
                    assertEquals(HttpMethod.Get, request.method)
                    assertEquals("/api/studios/42/memberships/students", request.url.encodedPath)
                    assertEquals("1", request.headers["X-API-Version"])
                    assertEquals("Bearer access-token", request.headers[HttpHeaders.Authorization])
                    assertEquals("10", request.url.parameters["size"])
                    when (requestIndex++) {
                        0 -> {
                            assertNull(request.url.parameters["cursor"])
                            respond(
                                """{"items":[{"id":1,"name":"김민수","phoneNumber":"01012345678","registered":true,"status":"ACTIVE"}],"hasNext":true,"nextCursor":"20"}""",
                                headers = jsonHeaders,
                            )
                        }

                        1 -> {
                            assertEquals("20", request.url.parameters["cursor"])
                            respond(
                                """{"items":[],"hasNext":false,"nextCursor":null}""",
                                headers = jsonHeaders,
                            )
                        }

                        else -> {
                            error("예상하지 못한 요청입니다: ${request.url}")
                        }
                    }
                }
            val client = createClient(engine)

            try {
                val api = InstructorMemberApi(client)
                val first = api.getStudents("42", null, 10)
                val second = api.getStudents("42", first.nextCursor, 10)

                assertEquals(1L, first.items.single().id)
                assertTrue(first.hasNext)
                assertEquals("20", first.nextCursor)
                assertFalse(second.hasNext)
                assertNull(second.nextCursor)
            } finally {
                client.close()
            }
        }

    @Test
    fun `회원 등록 상세 수정 삭제는 확정 body와 본문 없는 성공 응답을 사용한다`() =
        runBlocking {
            var requestIndex = 0
            val engine =
                MockEngine { request ->
                    assertEquals("1", request.headers["X-API-Version"])
                    assertEquals("Bearer access-token", request.headers[HttpHeaders.Authorization])
                    when (requestIndex++) {
                        0 -> {
                            assertEquals(HttpMethod.Post, request.method)
                            assertEquals("/api/studios/42/memberships/students", request.url.encodedPath)
                            assertJsonBody(request, "김민수", "01012345678")
                            respond("", status = HttpStatusCode.Created)
                        }

                        1 -> {
                            assertEquals(HttpMethod.Get, request.method)
                            assertEquals("/api/studios/42/memberships/7", request.url.encodedPath)
                            respond(
                                """{"id":7,"name":"김민수","phoneNumber":"01012345678","studioRole":{"id":2,"name":"회원","instructor":false},"registered":true,"status":"ACTIVE","joinedAt":"2026-08-14T10:00:00"}""",
                                headers = jsonHeaders,
                            )
                        }

                        2 -> {
                            assertEquals(HttpMethod.Patch, request.method)
                            assertEquals("/api/studios/42/memberships/7", request.url.encodedPath)
                            assertJsonBody(request, "김민수2", "01012345678")
                            respond("", status = HttpStatusCode.NoContent)
                        }

                        3 -> {
                            assertEquals(HttpMethod.Delete, request.method)
                            assertEquals("/api/studios/42/memberships/7", request.url.encodedPath)
                            respond("", status = HttpStatusCode.NoContent)
                        }

                        else -> {
                            error("예상하지 못한 요청입니다: ${request.url}")
                        }
                    }
                }
            val client = createClient(engine)

            try {
                val api = InstructorMemberApi(client)
                api.registerStudent("42", "김민수", "01012345678")
                val detail = api.getMembership("42", 7)
                api.updateStudent("42", 7, "김민수2", "01012345678")
                api.deleteMembership("42", 7)

                assertEquals(7L, detail.id)
                assertTrue(detail.registered)
                assertEquals("회원", detail.studioRole.name)
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

private suspend fun assertJsonBody(
    request: io.ktor.client.request.HttpRequestData,
    name: String,
    phoneNumber: String,
) {
    val body = assertNotNull(request.body as? OutgoingContent)
    assertEquals(ContentType.Application.Json, body.contentType)
    val json = request.body.toByteArray().decodeToString()
    assertTrue(json.contains("\"name\":\"$name\""))
    assertTrue(json.contains("\"phoneNumber\":\"$phoneNumber\""))
    assertFalse(json.contains("role"))
}
