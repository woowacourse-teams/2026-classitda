package com.classitda.data.remote.instructor.mypage.studio

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders

internal class StudioApi(
    private val client: HttpClient,
) {
    suspend fun getMine(): List<StudioResponseDto> =
        client
            .get("api/studios/me")
            .body()

    suspend fun get(studioId: Long): StudioResponseDto =
        client
            .get("api/studios/$studioId")
            .body()

    suspend fun create(request: StudioCreateRequestDto) {
        client.post("api/studios") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(request)
        }
    }

    suspend fun update(
        studioId: Long,
        request: StudioUpdateRequestDto,
    ) {
        client.patch("api/studios/$studioId") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(request)
        }
    }

    suspend fun deleteImage(studioId: Long) {
        client.delete("api/studios/$studioId/image")
    }
}
