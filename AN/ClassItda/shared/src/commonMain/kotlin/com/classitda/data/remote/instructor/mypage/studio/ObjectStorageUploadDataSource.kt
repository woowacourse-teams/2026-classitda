package com.classitda.data.remote.instructor.mypage.studio

import com.classitda.core.platform.StudioImageBinaryReadException
import com.classitda.core.platform.StudioImageBinaryReader
import com.classitda.domain.model.instructor.mypage.StudioImageSelection
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.utils.io.ByteWriteChannel
import kotlinx.coroutines.CancellationException

internal class ObjectStorageUploadDataSource(
    private val client: HttpClient,
    private val binaryReader: StudioImageBinaryReader,
) {
    suspend fun put(
        uploadUrl: String,
        contentType: String,
        sizeBytes: Long,
        image: StudioImageSelection.Local,
    ): ObjectStorageUploadResult =
        try {
            val response =
                client.put(uploadUrl) {
                    header(HttpHeaders.ContentLength, sizeBytes)
                    setBody(
                        object : OutgoingContent.WriteChannelContent() {
                            override val contentType: ContentType = ContentType.parse(contentType)
                            override val contentLength: Long = sizeBytes

                            override suspend fun writeTo(channel: ByteWriteChannel) {
                                binaryReader.writeTo(image.handle, channel)
                            }
                        },
                    )
                    contentType(ContentType.parse(contentType))
                    header(HttpHeaders.ContentType, contentType)
                }
            if (response.status.value in 200..299) {
                ObjectStorageUploadResult.Accepted
            } else {
                ObjectStorageUploadResult.Rejected
            }
        } catch (_: StudioImageBinaryReadException) {
            ObjectStorageUploadResult.ReadFailed
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Throwable) {
            ObjectStorageUploadResult.NetworkFailure
        }
}

internal sealed interface ObjectStorageUploadResult {
    data object Accepted : ObjectStorageUploadResult

    data object Rejected : ObjectStorageUploadResult

    data object ReadFailed : ObjectStorageUploadResult

    data object NetworkFailure : ObjectStorageUploadResult
}
