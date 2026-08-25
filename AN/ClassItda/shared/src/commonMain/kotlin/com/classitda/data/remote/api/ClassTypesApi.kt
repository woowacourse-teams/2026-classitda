package com.classitda.data.remote.api

import co.touchlab.kermit.Logger
import com.classitda.data.remote.dto.ClassTypeCreateRequestDto
import com.classitda.data.remote.dto.ClassTypeResponseDto
import com.classitda.data.remote.dto.ClassTypeUpdateRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class ClassTypesApi(
    private val client: HttpClient,
) {
    suspend fun getClassTypes(studioId: Long): List<ClassTypeResponseDto> {
        val response: List<ClassTypeResponseDto> = client.get("api/studios/$studioId/class-types").body()
        Logger.d("getClassTypes response: $response")
        return response
    }

    suspend fun createClassType(
        studioId: Long,
        request: ClassTypeCreateRequestDto,
    ) {
        val response =
            client.post("api/studios/$studioId/class-types") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        Logger.d("createClassType response: ${response.status}")
    }

    suspend fun editClassType(
        studioId: Long,
        classTypeId: Long,
        request: ClassTypeUpdateRequestDto,
    ) {
        val response =
            client.patch("api/studios/$studioId/class-types/$classTypeId") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        Logger.d("editClassType response: ${response.status}")
    }

    suspend fun deleteClassType(
        studioId: Long,
        classTypeId: Long,
    ) {
        val response = client.delete("api/studios/$studioId/class-types/$classTypeId")
        Logger.d("deleteClassType response: ${response.status}")
    }
}
