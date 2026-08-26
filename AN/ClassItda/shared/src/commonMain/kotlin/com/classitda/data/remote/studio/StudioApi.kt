package com.classitda.data.remote.studio

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

internal class StudioApi(
    private val client: HttpClient,
) {
    suspend fun getMyStudios(): List<StudioResponseDto> =
        client
            .get("api/studios/me")
            .body()
}
