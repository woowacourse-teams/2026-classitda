package com.classitda.data.repository.instructor.mypage

import com.classitda.core.network.createClassItdaHttpClient
import com.classitda.data.remote.instructor.mypage.facility.StudioApi
import com.classitda.data.remote.instructor.mypage.facility.StudioRemoteDataSource
import com.classitda.data.remote.instructor.mypage.facility.validStudioJson
import com.classitda.domain.model.instructor.mypage.FacilityImageSelection
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.repository.instructor.mypage.FacilityList
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class RemoteInstructorFacilityRepositoryTest {
    @Test
    fun `목록은 응답 순서와 구조화 주소 및 원격 이미지를 보존한다`() =
        runBlocking {
            val (repository, client) =
                repositoryWith {
                    respond(
                        "[${validStudioJson(2)},${validStudioJson(1, image = null)}]",
                        headers = jsonHeaders,
                    )
                }

            val result = assertIs<InstructorMyPageResult.Success<*>>(repository.getFacilities())
            val page = assertIs<FacilityList>(result.value)

            assertEquals(listOf("2", "1"), page.facilities.map { it.id.value })
            assertEquals(2, page.totalCount)
            with(page.facilities.first()) {
                assertEquals("13494", address.zoneCode)
                assertEquals("경기 성남시 분당구 판교역로 166", address.roadAddress)
                assertEquals("경기 성남시 분당구 백현동 532", address.jibunAddress)
                assertEquals("카카오 판교 아지트", address.buildingName)
                assertEquals("3층", address.detailAddress)
                assertEquals("09:00:00", openingTime)
                assertEquals("22:00:00", closingTime)
                assertEquals("시설 설명 2", description)
                assertIs<FacilityImageSelection.Remote>(image)
            }
            assertNull(page.facilities.last().image)
            client.close()
        }

    @Test
    fun `빈 목록 응답은 성공한 빈 FacilityList다`() =
        runBlocking {
            val (repository, client) = repositoryWith { respond("[]", headers = jsonHeaders) }

            val result = assertIs<InstructorMyPageResult.Success<*>>(repository.getFacilities())
            val page = assertIs<FacilityList>(result.value)

            assertEquals(0, page.totalCount)
            assertEquals(emptyList(), page.facilities)
            client.close()
        }

    @Test
    fun `상세 404는 NOT_FOUND로 변환한다`() =
        runBlocking {
            val (repository, client) =
                repositoryWith { respond("{}", status = HttpStatusCode.NotFound, headers = jsonHeaders) }

            val failure =
                assertIs<InstructorMyPageResult.Failure>(
                    repository.getFacility(InstructorFacilityId("42")),
                )

            assertEquals(InstructorMyPageFailureReason.NOT_FOUND, failure.reason)
            client.close()
        }

    @Test
    fun `숫자가 아닌 Domain ID는 네트워크를 호출하지 않는다`() =
        runBlocking {
            var requestCount = 0
            val (repository, client) =
                repositoryWith {
                    requestCount += 1
                    error("호출되면 안 됩니다")
                }

            val failure =
                assertIs<InstructorMyPageResult.Failure>(
                    repository.getFacility(InstructorFacilityId("facility-42")),
                )

            assertEquals(InstructorMyPageFailureReason.INVALID_REQUEST, failure.reason)
            assertEquals(0, requestCount)
            client.close()
        }

    @Test
    fun `HTTP 상태 코드를 Domain 오류로 구분한다`() =
        runBlocking {
            val cases =
                listOf(
                    HttpStatusCode.BadRequest to InstructorMyPageFailureReason.INVALID_REQUEST,
                    HttpStatusCode.Unauthorized to InstructorMyPageFailureReason.UNAUTHORIZED,
                    HttpStatusCode.Forbidden to InstructorMyPageFailureReason.FORBIDDEN,
                    HttpStatusCode.NotFound to InstructorMyPageFailureReason.NOT_FOUND,
                    HttpStatusCode.InternalServerError to InstructorMyPageFailureReason.SERVER,
                )

            for ((status, expectedReason) in cases) {
                val (repository, client) =
                    repositoryWith { respond("{}", status = status, headers = jsonHeaders) }

                val failure = assertIs<InstructorMyPageResult.Failure>(repository.getFacilities())
                assertEquals(expectedReason, failure.reason)
                client.close()
            }
        }

    @Test
    fun `네트워크와 직렬화 및 응답 계약 오류를 구분한다`() =
        runBlocking {
            val (networkRepository, networkClient) =
                repositoryWith { throw IOException("offline") }
            val networkFailure =
                assertIs<InstructorMyPageResult.Failure>(networkRepository.getFacilities())
            assertEquals(InstructorMyPageFailureReason.NETWORK, networkFailure.reason)
            networkClient.close()

            val (serializationRepository, serializationClient) =
                repositoryWith { respond("not-json", headers = jsonHeaders) }
            val serializationFailure =
                assertIs<InstructorMyPageResult.Failure>(serializationRepository.getFacilities())
            assertEquals(InstructorMyPageFailureReason.CONTRACT, serializationFailure.reason)
            serializationClient.close()

            val (contractRepository, contractClient) =
                repositoryWith {
                    respond(
                        validStudioJson(1).replace("\"name\":\"시설 1\",", ""),
                        headers = jsonHeaders,
                    )
                }
            val contractFailure =
                assertIs<InstructorMyPageResult.Failure>(
                    contractRepository.getFacility(InstructorFacilityId("1")),
                )
            assertEquals(InstructorMyPageFailureReason.CONTRACT, contractFailure.reason)
            contractClient.close()
        }

    private fun repositoryWith(handler: MockRequestHandler): Pair<RemoteInstructorFacilityRepository, HttpClient> {
        val client = createClassItdaHttpClient(MockEngine(handler), BASE_URL)
        val repository = RemoteInstructorFacilityRepository(StudioRemoteDataSource(StudioApi(client)))
        return repository to client
    }

    private companion object {
        const val BASE_URL = "https://api.classitda.test/"
        val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
    }
}
