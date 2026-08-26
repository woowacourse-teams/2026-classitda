package com.classitda.data.remote.member

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class MemberApi(
    private val client: HttpClient,
) {
    suspend fun getMe(): MemberMeResponseDto =
        client
            .get("api/members/me")
            .body()

    suspend fun updateName(name: String) {
        client.patch("api/members/me/name") {
            contentType(ContentType.Application.Json)
            setBody(MyNameUpdateRequestDto(name))
        }
    }
}
