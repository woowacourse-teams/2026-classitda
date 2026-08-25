package com.classitda.data.repository.instructor.mypage

import com.classitda.core.network.createClassItdaHttpClient
import com.classitda.data.remote.instructor.mypage.facility.StudioApi
import com.classitda.data.remote.instructor.mypage.facility.StudioRemoteDataSource
import com.classitda.domain.model.instructor.mypage.FacilityAddress
import com.classitda.domain.model.instructor.mypage.FacilityImageSelection
import com.classitda.domain.model.instructor.mypage.FacilityRegistrationDraft
import com.classitda.domain.model.instructor.mypage.UploadedFacilityImage
import com.classitda.domain.repository.instructor.mypage.FacilityImageUploader
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RemoteInstructorFacilityCreationTest {
    @Test
    fun `Local 이미지는 업로드 후 objectKey로 생성하고 201 no body를 성공 처리한다`() =
        runBlocking {
            val events = mutableListOf<String>()
            val uploader =
                RecordingImageUploader(
                    InstructorMyPageResult.Success(UploadedFacilityImage("images/studio.jpg")),
                ) {
                    events += "upload"
                }
            val (repository, client) =
                repositoryWith(uploader) { request ->
                    events += "create"
                    assertEquals(HttpMethod.Post, request.method)
                    val body = request.body.toByteArray().decodeToString()
                    assertTrue(body.contains("\"image\":\"images/studio.jpg\""))
                    assertFalse(body.contains("opaque-handle"))
                    respond("", status = HttpStatusCode.Created)
                }

            val result = repository.registerFacility(validDraft(localImage()))

            assertEquals(InstructorMyPageResult.Success(Unit), result)
            assertEquals(listOf("upload", "create"), events)
            client.close()
        }

    @Test
    fun `이미지가 없으면 생성 JSON에서 image를 생략한다`() =
        runBlocking {
            val (repository, client) =
                repositoryWith(null) { request ->
                    val body = request.body.toByteArray().decodeToString()
                    assertFalse(body.contains("\"image\""))
                    respond("", status = HttpStatusCode.Created)
                }

            val result = repository.registerFacility(validDraft())

            assertEquals(InstructorMyPageResult.Success(Unit), result)
            client.close()
        }

    @Test
    fun `이미지 업로드가 실패하면 시설 생성 POST를 호출하지 않는다`() =
        runBlocking {
            var createCalls = 0
            val uploader =
                RecordingImageUploader(
                    InstructorMyPageResult.Failure(InstructorMyPageFailureReason.IMAGE_READ_FAILED),
                )
            val (repository, client) =
                repositoryWith(uploader) {
                    createCalls += 1
                    error("업로드 실패 후 호출되면 안 됩니다")
                }

            val failure =
                assertIs<InstructorMyPageResult.Failure>(
                    repository.registerFacility(validDraft(localImage())),
                )

            assertEquals(InstructorMyPageFailureReason.IMAGE_READ_FAILED, failure.reason)
            assertEquals(0, createCalls)
            client.close()
        }

    @Test
    fun `생성 POST가 실패한 뒤 같은 제출을 재시도하면 재업로드하지 않는다`() =
        runBlocking {
            var createCalls = 0
            val uploader =
                RecordingImageUploader(InstructorMyPageResult.Success(UploadedFacilityImage("images/reused.jpg")))
            val (repository, client) =
                repositoryWith(uploader) {
                    createCalls += 1
                    if (createCalls == 1) {
                        respond("", status = HttpStatusCode.InternalServerError)
                    } else {
                        val body = it.body.toByteArray().decodeToString()
                        assertTrue(body.contains("\"image\":\"images/reused.jpg\""))
                        respond("", status = HttpStatusCode.Created)
                    }
                }
            val draft = validDraft(localImage())

            val firstFailure = assertIs<InstructorMyPageResult.Failure>(repository.registerFacility(draft))
            val secondResult = repository.registerFacility(draft)

            assertEquals(InstructorMyPageFailureReason.SERVER, firstFailure.reason)
            assertEquals(InstructorMyPageResult.Success(Unit), secondResult)
            assertEquals(1, uploader.callCount)
            assertEquals(2, createCalls)
            client.close()
        }

    @Test
    fun `생성 오류 코드는 Domain의 잘못된 요청과 충돌로 구분한다`() =
        runBlocking {
            val invalidCodes = listOf("COMMON-001", "STUDIO-001", "STUDIO-007", "API-001")
            for (code in invalidCodes) {
                val (repository, client) =
                    repositoryWith(null) {
                        respond(
                            "{\"code\":\"$code\"}",
                            status = HttpStatusCode.BadRequest,
                            headers = headersOf("Content-Type", "application/json"),
                        )
                    }

                val failure = assertIs<InstructorMyPageResult.Failure>(repository.registerFacility(validDraft()))

                assertEquals(InstructorMyPageFailureReason.INVALID_REQUEST, failure.reason)
                client.close()
            }

            val (repository, client) =
                repositoryWith(null) {
                    respond(
                        "{\"code\":\"STUDIO-008\"}",
                        status = HttpStatusCode.Conflict,
                        headers = headersOf("Content-Type", "application/json"),
                    )
                }

            val failure = assertIs<InstructorMyPageResult.Failure>(repository.registerFacility(validDraft()))

            assertEquals(InstructorMyPageFailureReason.CONFLICT, failure.reason)
            client.close()
        }

    private fun repositoryWith(
        uploader: FacilityImageUploader?,
        handler: io.ktor.client.engine.mock.MockRequestHandler,
    ): Pair<RemoteInstructorFacilityRepository, io.ktor.client.HttpClient> {
        val client = createClassItdaHttpClient(MockEngine(handler), BASE_URL)
        return RemoteInstructorFacilityRepository(StudioRemoteDataSource(StudioApi(client)), uploader) to client
    }

    private fun validDraft(image: FacilityImageSelection? = null) =
        FacilityRegistrationDraft(
            image = image,
            name = "클래스잇다 스튜디오",
            address =
                FacilityAddress(
                    zoneCode = "13494",
                    roadAddress = "경기 성남시 분당구 판교역로 166",
                    jibunAddress = "경기 성남시 분당구 백현동 532",
                    buildingName = "카카오 판교 아지트",
                    detailAddress = "3층",
                ),
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
        private val result: InstructorMyPageResult<UploadedFacilityImage>,
        private val onUpload: () -> Unit = {},
    ) : FacilityImageUploader {
        var callCount: Int = 0
            private set

        override suspend fun upload(
            image: FacilityImageSelection.Local,
        ): InstructorMyPageResult<UploadedFacilityImage> {
            callCount += 1
            onUpload()
            return result
        }
    }

    private companion object {
        const val BASE_URL = "https://api.classitda.test/"
    }
}
