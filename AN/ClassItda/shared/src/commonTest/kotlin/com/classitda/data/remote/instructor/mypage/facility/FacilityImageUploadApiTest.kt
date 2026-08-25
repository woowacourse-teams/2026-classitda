package com.classitda.data.remote.instructor.mypage.facility

import com.classitda.core.auth.InMemoryAuthTokenStorage
import com.classitda.core.network.createClassItdaHttpClient
import com.classitda.domain.model.auth.signup.LoginTokens
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FacilityImageUploadApiTest {
    @Test
    fun `이미지 업로드 URL 발급은 확정 path body 인증 header를 사용한다`() =
        runBlocking {
            val engine =
                MockEngine { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    assertEquals("/api/studios/image-upload-url", request.url.encodedPath)
                    assertEquals("1", request.headers["X-API-Version"])
                    assertEquals("Bearer access-token", request.headers[HttpHeaders.Authorization])
                    val body = request.body.toByteArray().decodeToString()
                    assertTrue(body.contains("\"extension\":\"jpg\""))
                    assertTrue(body.contains("\"size\":3145728"))
                    respond(
                        """
                        {
                          "objectKey":"studios/images/uploaded.jpg",
                          "uploadUrl":"https://storage.test/upload",
                          "contentType":"image/jpeg"
                        }
                        """.trimIndent(),
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val tokenStorage = InMemoryAuthTokenStorage().apply { write(testTokens) }
            val client = createClassItdaHttpClient(engine, BASE_URL, tokenStorage)

            try {
                val response =
                    FacilityImageUploadApi(client).issueUrl(
                        ImageUploadUrlRequestDto(extension = "jpg", size = 3L * 1024L * 1024L),
                    )

                assertEquals("studios/images/uploaded.jpg", response.objectKey)
                assertEquals("https://storage.test/upload", response.uploadUrl)
                assertEquals("image/jpeg", response.contentType)
            } finally {
                client.close()
            }
        }

    private companion object {
        const val BASE_URL = "https://api.classitda.test/"
        val testTokens =
            LoginTokens(
                accessToken = "access-token",
                accessTokenExpiresInSeconds = 3600,
                refreshToken = "refresh-token",
                refreshTokenExpiresInSeconds = 86400,
            )
    }
}
