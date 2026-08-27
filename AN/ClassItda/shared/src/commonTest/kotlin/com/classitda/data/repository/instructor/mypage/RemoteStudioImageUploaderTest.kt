package com.classitda.data.repository.instructor.mypage

import com.classitda.core.network.createClassItdaHttpClient
import com.classitda.core.network.createObjectStorageHttpClient
import com.classitda.core.platform.StudioImageBinaryReadException
import com.classitda.core.platform.StudioImageBinaryReader
import com.classitda.data.remote.instructor.mypage.studio.ObjectStorageUploadDataSource
import com.classitda.data.remote.instructor.mypage.studio.StudioImageUploadApi
import com.classitda.domain.model.instructor.mypage.StudioImageSelection
import com.classitda.domain.model.instructor.mypage.UploadedStudioImage
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RemoteStudioImageUploaderTest {
    @Test
    fun `presigned PUT은 raw body와 발급된 content type size만 사용하고 성공 시 objectKey를 반환한다`() =
        runBlocking {
            var issueBody = ""
            val issueClient =
                createClassItdaHttpClient(
                    MockEngine { request ->
                        issueBody = request.body.toByteArray().decodeToString()
                        respond(
                            """
                            {"objectKey":"object-key","uploadUrl":"https://storage.test/upload","contentType":"image/jpeg"}
                            """.trimIndent(),
                            headers = jsonHeaders,
                        )
                    },
                    BASE_URL,
                )
            var putMethod: HttpMethod? = null
            var putUrl = ""
            var putBodyContentType: String? = null
            var putContentLength: String? = null
            var putAuthorization: String? = null
            var putApiVersion: String? = null
            var putBody = ""
            val objectClient =
                createObjectStorageHttpClient(
                    MockEngine { request ->
                        putMethod = request.method
                        putUrl = request.url.toString()
                        putContentLength = request.headers[HttpHeaders.ContentLength]
                        putAuthorization = request.headers[HttpHeaders.Authorization]
                        putApiVersion = request.headers["X-API-Version"]
                        putBodyContentType = request.body.contentType?.toString()
                        putBody = request.body.toByteArray().decodeToString()
                        respond("", status = HttpStatusCode.OK)
                    },
                )
            var releaseCount = 0
            val image = localImage(sizeBytes = 4)

            try {
                val result =
                    RemoteStudioImageUploader(
                        uploadApi = StudioImageUploadApi(issueClient),
                        objectStorage = ObjectStorageUploadDataSource(objectClient, TestReader("raw!")),
                        releaseLocalImage = { releaseCount += 1 },
                    ).upload(image)

                assertEquals(
                    InstructorMyPageResult.Success(UploadedStudioImage("object-key")),
                    result,
                )
                assertEquals("\"extension\":\"jpg\"", issueBody.substringAfter("{").substringBefore(",\"size"))
                assertTrue(issueBody.contains("\"size\":4"))
                assertEquals(HttpMethod.Put, putMethod)
                assertEquals("https://storage.test/upload", putUrl)
                assertEquals("image/jpeg", putBodyContentType)
                assertEquals("4", putContentLength)
                assertNull(putAuthorization)
                assertNull(putApiVersion)
                assertEquals("raw!", putBody)
                assertEquals(1, releaseCount)
            } finally {
                issueClient.close()
                objectClient.close()
            }
        }

    @Test
    fun `PUT 거부는 새 URL을 한 번 발급해 재시도하고 두 번째도 거부되면 전용 오류가 된다`() =
        runBlocking {
            var issueCount = 0
            var putCount = 0
            val issueClient =
                createClassItdaHttpClient(
                    MockEngine {
                        issueCount += 1
                        respond(
                            """{"objectKey":"key-$issueCount","uploadUrl":"https://storage.test/$issueCount","contentType":"image/png"}""",
                            headers = jsonHeaders,
                        )
                    },
                    BASE_URL,
                )
            val objectClient =
                createObjectStorageHttpClient(
                    MockEngine {
                        putCount += 1
                        respond("", status = HttpStatusCode.Forbidden)
                    },
                )

            try {
                val result =
                    RemoteStudioImageUploader(
                        StudioImageUploadApi(issueClient),
                        ObjectStorageUploadDataSource(objectClient, TestReader("raw")),
                        releaseLocalImage = {},
                    ).upload(localImage(mimeType = "image/png", fileName = "photo.png"))

                assertEquals(
                    InstructorMyPageResult.Failure(InstructorMyPageFailureReason.UPLOAD_EXPIRED_OR_REJECTED),
                    result,
                )
                assertEquals(2, issueCount)
                assertEquals(2, putCount)
            } finally {
                issueClient.close()
                objectClient.close()
            }
        }

    @Test
    fun `지원하지 않는 이미지와 5MB 초과 이미지는 URL 발급 전에 거부한다`() =
        runBlocking {
            var issueCount = 0
            val issueClient =
                createClassItdaHttpClient(
                    MockEngine {
                        issueCount += 1
                        error("URL을 발급하면 안 됩니다")
                    },
                    BASE_URL,
                )
            val objectClient = createObjectStorageHttpClient(MockEngine { error("PUT하면 안 됩니다") })

            try {
                val uploader =
                    RemoteStudioImageUploader(
                        StudioImageUploadApi(issueClient),
                        ObjectStorageUploadDataSource(objectClient, TestReader("raw")),
                        releaseLocalImage = {},
                    )

                val unsupported = uploader.upload(localImage(mimeType = "image/heic", fileName = "photo.heic"))
                val oversized = uploader.upload(localImage(sizeBytes = 5L * 1024L * 1024L + 1L))

                assertEquals(
                    InstructorMyPageResult.Failure(InstructorMyPageFailureReason.UNSUPPORTED_IMAGE),
                    unsupported,
                )
                assertEquals(
                    InstructorMyPageResult.Failure(InstructorMyPageFailureReason.IMAGE_TOO_LARGE),
                    oversized,
                )
                assertEquals(0, issueCount)
            } finally {
                issueClient.close()
                objectClient.close()
            }
        }

    @Test
    fun `발급 응답 필수값 누락은 PUT 없이 CONTRACT 오류가 된다`() =
        runBlocking {
            var putCount = 0
            val issueClient =
                createClassItdaHttpClient(
                    MockEngine {
                        respond(
                            """{"objectKey":"key","uploadUrl":null,"contentType":"image/jpeg"}""",
                            headers = jsonHeaders,
                        )
                    },
                    BASE_URL,
                )
            val objectClient =
                createObjectStorageHttpClient(
                    MockEngine {
                        putCount += 1
                        respond("", status = HttpStatusCode.OK)
                    },
                )

            try {
                val result =
                    RemoteStudioImageUploader(
                        StudioImageUploadApi(issueClient),
                        ObjectStorageUploadDataSource(objectClient, TestReader("raw")),
                        releaseLocalImage = {},
                    ).upload(localImage())

                assertEquals(InstructorMyPageResult.Failure(InstructorMyPageFailureReason.CONTRACT), result)
                assertEquals(0, putCount)
            } finally {
                issueClient.close()
                objectClient.close()
            }
        }

    @Test
    fun `업로드 URL 발급 409는 CONFLICT로 매핑한다`() =
        runBlocking {
            val issueClient =
                createClassItdaHttpClient(
                    MockEngine {
                        respond("", status = HttpStatusCode.Conflict)
                    },
                    BASE_URL,
                )
            val objectClient = createObjectStorageHttpClient(MockEngine { error("PUT하면 안 됩니다") })

            try {
                val result =
                    RemoteStudioImageUploader(
                        StudioImageUploadApi(issueClient),
                        ObjectStorageUploadDataSource(objectClient, TestReader("raw")),
                        releaseLocalImage = {},
                    ).upload(localImage())

                assertEquals(
                    InstructorMyPageResult.Failure(InstructorMyPageFailureReason.CONFLICT),
                    result,
                )
            } finally {
                issueClient.close()
                objectClient.close()
            }
        }

    @Test
    fun `reader boundary는 읽기 실패를 전용 예외로 표현한다`() =
        runBlocking {
            try {
                FailingReader.writeTo("handle", ByteChannel(autoFlush = true))
                error("reader가 실패하지 않았습니다")
            } catch (_: StudioImageBinaryReadException) {
                // Expected: the upload data source maps this boundary failure to IMAGE_READ_FAILED.
            }
        }

    private fun localImage(
        mimeType: String = "image/jpeg",
        fileName: String = "photo.jpg",
        sizeBytes: Long = 4,
    ): StudioImageSelection.Local =
        StudioImageSelection.Local(
            handle = "classitda-studio-image-test",
            previewReference = "preview",
            mimeType = mimeType,
            fileName = fileName,
            sizeBytes = sizeBytes,
        )

    private class TestReader(
        private val content: String,
    ) : StudioImageBinaryReader {
        override suspend fun writeTo(
            handle: String,
            sink: ByteWriteChannel,
        ) {
            sink.writeFully(content.encodeToByteArray())
        }
    }

    private object FailingReader : StudioImageBinaryReader {
        override suspend fun writeTo(
            handle: String,
            sink: ByteWriteChannel,
        ): Unit = throw StudioImageBinaryReadException()
    }

    private companion object {
        const val BASE_URL = "https://api.classitda.test/"
        val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
    }
}
