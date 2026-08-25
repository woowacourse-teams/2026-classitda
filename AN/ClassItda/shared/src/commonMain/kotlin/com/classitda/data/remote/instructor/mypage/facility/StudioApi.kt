package com.classitda.data.remote.instructor.mypage.facility

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

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
}
