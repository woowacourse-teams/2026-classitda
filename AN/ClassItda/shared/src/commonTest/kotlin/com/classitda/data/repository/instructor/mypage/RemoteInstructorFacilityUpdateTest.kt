package com.classitda.data.repository.instructor.mypage

import com.classitda.core.network.createClassItdaHttpClient
import com.classitda.data.remote.instructor.mypage.facility.StudioApi
import com.classitda.data.remote.instructor.mypage.facility.StudioRemoteDataSource
import com.classitda.domain.model.instructor.mypage.FacilityAddress
import com.classitda.domain.model.instructor.mypage.FacilityImageMutation
import com.classitda.domain.model.instructor.mypage.FacilityImageSelection
import com.classitda.domain.model.instructor.mypage.FacilityRegistrationDraft
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.ManagedFacility
import com.classitda.domain.model.instructor.mypage.UploadedFacilityImage
import com.classitda.domain.repository.instructor.mypage.FacilityImageUploader
import com.classitda.domain.repository.instructor.mypage.FacilityUpdateOperation
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RemoteInstructorFacilityUpdateTest {
    @Test
    fun `일반 필드 수정은 변경 필드만 PATCH하고 204를 성공 처리한다`() =
        runBlocking {
            val original = originalFacility()
            val draft = draft(image = original.image).copy(name = "변경 시설")
            val (repository, client) =
                repositoryWith { request ->
                    assertEquals(HttpMethod.Patch, request.method)
                    assertEquals("/api/studios/42", request.url.encodedPath)
                    val body = request.body.toByteArray().decodeToString()
                    assertTrue(body.contains("\"name\":\"변경 시설\""))
                    assertFalse(body.contains("address"))
                    assertFalse(body.contains("phoneNumber"))
                    assertFalse(body.contains("image"))
                    respond("", status = HttpStatusCode.NoContent)
                }

            val result =
                repository.updateFacility(
                    original.id,
                    original,
                    draft,
                    FacilityImageMutation.Unchanged,
                )

            assertEquals(InstructorMyPageResult.Success(Unit), result)
            client.close()
        }

    @Test
    fun `주소 변경은 다섯 주소 값을 포함한 PATCH를 보낸다`() =
        runBlocking {
            val original = originalFacility()
            val changedAddress =
                original.address.copy(
                    zoneCode = "13529",
                    roadAddress = "서울특별시 강남구 테헤란로 1",
                    jibunAddress = "서울특별시 강남구 역삼동 1",
                    buildingName = "새 빌딩",
                    detailAddress = "2층",
                )
            val (repository, client) =
                repositoryWith { request ->
                    val body = request.body.toByteArray().decodeToString()
                    assertTrue(body.contains("\"zonecode\":\"13529\""))
                    assertTrue(body.contains("\"roadAddress\":\"서울특별시 강남구 테헤란로 1\""))
                    assertTrue(body.contains("\"jibunAddress\":\"서울특별시 강남구 역삼동 1\""))
                    assertTrue(body.contains("\"buildingName\":\"새 빌딩\""))
                    assertTrue(body.contains("\"detailAddress\":\"2층\""))
                    respond("", status = HttpStatusCode.NoContent)
                }

            val result =
                repository.updateFacility(
                    original.id,
                    original,
                    draft(image = original.image).copy(address = changedAddress),
                    FacilityImageMutation.Unchanged,
                )

            assertEquals(InstructorMyPageResult.Success(Unit), result)
            client.close()
        }

    @Test
    fun `Local 교체는 업로드 후 objectKey로 PATCH하고 재시도 시 재업로드하지 않는다`() =
        runBlocking {
            val local = localImage()
            var patchCalls = 0
            val events = mutableListOf<String>()
            val uploader = RecordingImageUploader { events += "upload" }
            val (repository, client) =
                repositoryWith(uploader) { request ->
                    events += "patch"
                    patchCalls += 1
                    assertEquals(HttpMethod.Patch, request.method)
                    val body = request.body.toByteArray().decodeToString()
                    assertTrue(body.contains("\"image\":\"images/new.jpg\""))
                    assertFalse(body.contains(local.handle))
                    if (patchCalls == 1) {
                        respond("", status = HttpStatusCode.InternalServerError)
                    } else {
                        respond("", status = HttpStatusCode.NoContent)
                    }
                }
            val original = originalFacility()
            val draft = draft(image = local)

            val firstFailure =
                assertIs<InstructorMyPageResult.Failure>(
                    repository.updateFacility(
                        original.id,
                        original,
                        draft,
                        FacilityImageMutation.Replace(local),
                    ),
                )
            val secondResult =
                repository.updateFacility(
                    original.id,
                    original,
                    draft,
                    FacilityImageMutation.Replace(local),
                )

            assertEquals(InstructorMyPageFailureReason.SERVER, firstFailure.reason)
            assertEquals(InstructorMyPageResult.Success(Unit), secondResult)
            assertEquals(1, uploader.callCount)
            assertEquals(listOf("upload", "patch", "patch"), events)
            client.close()
        }

    @Test
    fun `Remove는 PATCH 후 DELETE를 호출하고 DELETE 실패 재시도에서 PATCH를 반복하지 않는다`() =
        runBlocking {
            val events = mutableListOf<String>()
            var deleteCalls = 0
            val (repository, client) =
                repositoryWith { request ->
                    when (request.method) {
                        HttpMethod.Patch -> {
                            events += "patch"
                            val body = request.body.toByteArray().decodeToString()
                            assertFalse(body.contains("image"))
                            respond("", status = HttpStatusCode.NoContent)
                        }

                        HttpMethod.Delete -> {
                            events += "delete"
                            deleteCalls += 1
                            if (deleteCalls == 1) {
                                respond("", status = HttpStatusCode.InternalServerError)
                            } else {
                                respond("", status = HttpStatusCode.NoContent)
                            }
                        }

                        else -> {
                            error("예상하지 못한 HTTP method입니다: ${request.method}")
                        }
                    }
                }
            val original = originalFacility()
            val draft = draft(image = null).copy(name = "변경 시설")

            val firstFailure =
                assertIs<InstructorMyPageResult.Failure>(
                    repository.updateFacility(
                        original.id,
                        original,
                        draft,
                        FacilityImageMutation.Remove,
                    ),
                )
            val secondResult =
                repository.updateFacility(
                    original.id,
                    original,
                    draft,
                    FacilityImageMutation.Remove,
                )

            assertEquals(InstructorMyPageFailureReason.SERVER, firstFailure.reason)
            assertEquals(
                setOf(FacilityUpdateOperation.PATCH),
                firstFailure.completedFacilityUpdateOperations,
            )
            assertEquals(InstructorMyPageResult.Success(Unit), secondResult)
            assertEquals(listOf("patch", "delete", "delete"), events)
            client.close()
        }

    @Test
    fun `대표 이미지 DELETE의 204 성공과 잘못된 ID의 사전 차단을 검증한다`() =
        runBlocking {
            var requestCount = 0
            val (repository, client) =
                repositoryWith { request ->
                    requestCount += 1
                    assertEquals(HttpMethod.Delete, request.method)
                    respond("", status = HttpStatusCode.NoContent)
                }
            val original = originalFacility()

            val result =
                repository.updateFacility(
                    original.id,
                    original,
                    draft(image = null),
                    FacilityImageMutation.Remove,
                )
            val invalid =
                repository.updateFacility(
                    InstructorFacilityId("facility-42"),
                    original,
                    draft(image = null),
                    FacilityImageMutation.Remove,
                )

            assertEquals(InstructorMyPageResult.Success(Unit), result)
            assertEquals(
                InstructorMyPageFailureReason.INVALID_REQUEST,
                assertIs<InstructorMyPageResult.Failure>(invalid).reason,
            )
            assertEquals(1, requestCount)
            client.close()
        }

    @Test
    fun `수정 오류 코드는 권한 없음 존재하지 않음 충돌과 잘못된 요청으로 구분한다`() =
        runBlocking {
            val cases =
                listOf(
                    Triple("PERMISSION-001", HttpStatusCode.Forbidden, InstructorMyPageFailureReason.FORBIDDEN),
                    Triple("MEMBERSHIP-001", HttpStatusCode.Forbidden, InstructorMyPageFailureReason.FORBIDDEN),
                    Triple("STUDIO-002", HttpStatusCode.NotFound, InstructorMyPageFailureReason.NOT_FOUND),
                    Triple("STUDIO-008", HttpStatusCode.Conflict, InstructorMyPageFailureReason.CONFLICT),
                    Triple("STUDIO-007", HttpStatusCode.BadRequest, InstructorMyPageFailureReason.INVALID_REQUEST),
                )
            for ((code, status, expectedReason) in cases) {
                val (repository, client) =
                    repositoryWith {
                        respond(
                            "{\"code\":\"$code\"}",
                            status = status,
                        )
                    }
                val result =
                    repository.updateFacility(
                        originalFacility().id,
                        originalFacility(),
                        draft(image = originalFacility().image).copy(name = "변경 시설"),
                        FacilityImageMutation.Unchanged,
                    )

                val failure = assertIs<InstructorMyPageResult.Failure>(result)
                assertEquals(expectedReason, failure.reason)
                client.close()
            }
        }

    private fun repositoryWith(
        uploader: FacilityImageUploader? = null,
        handler: io.ktor.client.engine.mock.MockRequestHandler,
    ): Pair<RemoteInstructorFacilityRepository, io.ktor.client.HttpClient> {
        val client =
            com.classitda.core.network.createClassItdaHttpClient(
                MockEngine(handler),
                BASE_URL,
            )
        return RemoteInstructorFacilityRepository(StudioRemoteDataSource(StudioApi(client)), uploader) to client
    }

    private fun originalFacility() =
        ManagedFacility(
            id = InstructorFacilityId("42"),
            name = "클래스잇다 스튜디오",
            address =
                FacilityAddress(
                    zoneCode = "13494",
                    roadAddress = "경기 성남시 분당구 판교역로 166",
                    jibunAddress = "경기 성남시 분당구 백현동 532",
                    buildingName = "카카오 판교 아지트",
                    detailAddress = "3층",
                ),
            image = FacilityImageSelection.Remote("https://cdn.classitda.com/studio.jpg"),
            phoneNumber = "031-123-4567",
            description = "시설 설명",
            openingTime = "09:00",
            closingTime = "22:00",
        )

    private fun draft(image: FacilityImageSelection?) =
        FacilityRegistrationDraft(
            image = image,
            name = "클래스잇다 스튜디오",
            address = originalFacility().address,
            phoneNumber = "031-123-4567",
            description = "시설 설명",
            openingTime = "09:00",
            closingTime = "22:00",
        )

    private fun localImage() =
        FacilityImageSelection.Local(
            handle = "opaque-handle",
            previewReference = "preview-reference",
            mimeType = "image/jpeg",
            fileName = "facility.jpg",
            sizeBytes = 1024,
        )

    private class RecordingImageUploader(
        private val onUpload: () -> Unit = {},
    ) : FacilityImageUploader {
        var callCount: Int = 0
            private set

        override suspend fun upload(
            image: FacilityImageSelection.Local,
        ): InstructorMyPageResult<UploadedFacilityImage> {
            callCount += 1
            onUpload()
            return InstructorMyPageResult.Success(UploadedFacilityImage("images/new.jpg"))
        }
    }

    private companion object {
        const val BASE_URL = "https://api.classitda.test/"
    }
}
