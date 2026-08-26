package com.classitda.data.remote.member

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

internal class MemberApi(
    private val client: HttpClient,
) {
    suspend fun getMe(): MemberMeResponseDto =
        client
            .get("api/members/me")
            .body()
}
