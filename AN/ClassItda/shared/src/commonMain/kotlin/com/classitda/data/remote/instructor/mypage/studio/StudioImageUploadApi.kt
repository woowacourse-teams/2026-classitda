package com.classitda.data.remote.instructor.mypage.studio

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class StudioImageUploadApi(
    private val client: HttpClient,
) {
    suspend fun issueUrl(request: ImageUploadUrlRequestDto): ImageUploadUrlResponseDto =
        client
            .post("api/studios/image-upload-url") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
}
