package com.classitda.data.remote.instructor.mypage.facility

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
import kotlin.test.assertTrue

class StudioApiTest {
    @Test
    fun `목록과 상세 GET은 확정 path와 공용 인증 header를 사용한다`() =
        runBlocking {
            var requestIndex = 0
            val engine =
                MockEngine { request ->
                    assertEquals(HttpMethod.Get, request.method)
                    assertEquals("1", request.headers["X-API-Version"])
                    assertEquals("Bearer access-token", request.headers[HttpHeaders.Authorization])

                    when (requestIndex++) {
                        0 -> {
                            assertEquals("/api/studios/me", request.url.encodedPath)
                            respond("[]", headers = jsonHeaders)
                        }

                        1 -> {
                            assertEquals("/api/studios/42", request.url.encodedPath)
                            respond(validStudioJson(42), headers = jsonHeaders)
                        }

                        else -> {
                            error("예상하지 못한 요청입니다: ${request.url}")
                        }
                    }
                }
            val tokenStorage = InMemoryAuthTokenStorage().apply { write(testTokens) }
            val client = createClassItdaHttpClient(engine, BASE_URL, tokenStorage)
            val api = StudioApi(client)

            assertEquals(emptyList(), api.getMine())
            assertEquals(42L, api.get(42).id)
            assertEquals(2, requestIndex)

            client.close()
        }

    @Test
    fun `생성은 POST와 전체 생성 JSON을 사용하고 201 no body를 Unit으로 처리한다`() =
        runBlocking {
            val engine =
                MockEngine { request ->
                    assertEquals(HttpMethod.Post, request.method)
                    assertEquals("/api/studios", request.url.encodedPath)
                    assertEquals("1", request.headers["X-API-Version"])
                    assertEquals("Bearer access-token", request.headers[HttpHeaders.Authorization])
                    val outgoingBody = assertIs<OutgoingContent>(request.body)
                    assertEquals(ContentType.Application.Json, outgoingBody.contentType)
                    val body = request.body.toByteArray().decodeToString()
                    assertTrue(body.contains("\"zonecode\":\"13494\""))
                    assertTrue(body.contains("\"roadAddress\":\"경기 성남시 분당구 판교역로 166\""))
                    assertTrue(body.contains("\"jibunAddress\":\"경기 성남시 분당구 백현동 532\""))
                    assertTrue(body.contains("\"buildingName\":\"카카오 판교 아지트\""))
                    assertTrue(body.contains("\"detailAddress\":\"3층\""))
                    assertTrue(body.contains("\"image\":\"studios/images/object-key.jpg\""))
                    respond("", status = HttpStatusCode.Created)
                }
            val tokenStorage = InMemoryAuthTokenStorage().apply { write(testTokens) }
            val client = createClassItdaHttpClient(engine, BASE_URL, tokenStorage)

            try {
                StudioApi(client).create(
                    StudioCreateRequestDto(
                        name = "클래스잇다 스튜디오",
                        address =
                            AddressRequestDto(
                                zoneCode = "13494",
                                roadAddress = "경기 성남시 분당구 판교역로 166",
                                jibunAddress = "경기 성남시 분당구 백현동 532",
                                buildingName = "카카오 판교 아지트",
                                detailAddress = "3층",
                            ),
                        phoneNumber = "031-123-4567",
                        openTime = "09:00",
                        closeTime = "22:00",
                        image = "studios/images/object-key.jpg",
                        description = "시설 설명",
                    ),
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

internal fun validStudioJson(
    id: Long,
    image: String? = "https://cdn.classitda.test/studios/$id.webp",
): String =
    """
    {
      "id":$id,
      "name":"시설 $id",
      "address":{
        "zonecode":"13494",
        "roadAddress":"경기 성남시 분당구 판교역로 166",
        "jibunAddress":"경기 성남시 분당구 백현동 532",
        "buildingName":"카카오 판교 아지트",
        "detailAddress":"3층"
      },
      "phoneNumber":"031-123-4567",
      "openTime":"09:00:00",
      "closeTime":"22:00:00",
      "image":${image?.let { "\"$it\"" } ?: "null"},
      "description":"시설 설명 $id",
      "futureField":"ignored"
    }
    """.trimIndent()
