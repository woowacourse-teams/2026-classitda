package com.classitda.data.remote.api

import co.touchlab.kermit.Logger
import com.classitda.data.remote.dto.ClassTemplateCreateRequestDto
import com.classitda.data.remote.dto.ClassTemplateResponseDto
import com.classitda.data.remote.dto.ClassTemplateUpdateRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class ClassTemplatesApi(
    private val client: HttpClient,
) {
    suspend fun getClassTemplates(studioId: Long): List<ClassTemplateResponseDto> {
        val response: List<ClassTemplateResponseDto> = client.get("api/studios/$studioId/class-templates").body()
        Logger.d("getClassTemplates response: $response")
        return response
    }

    suspend fun createClassTemplate(
        studioId: Long,
        request: ClassTemplateCreateRequestDto,
    ): ClassTemplateResponseDto {
        val response: ClassTemplateResponseDto =
            client
                .post("api/studios/$studioId/class-templates") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body()
        Logger.d("createClassTemplate response: $response")
        return response
    }

    suspend fun editClassTemplate(
        studioId: Long,
        classTemplateId: Long,
        request: ClassTemplateUpdateRequestDto,
    ) {
        val response =
            client.put("api/studios/$studioId/class-templates/$classTemplateId") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        Logger.d("editClassTemplate response: ${response.status}")
    }

    suspend fun deleteClassTemplate(
        studioId: Long,
        classTemplateId: Long,
    ) {
        val response = client.delete("api/studios/$studioId/class-templates/$classTemplateId")
        Logger.d("deleteClassTemplate response: ${response.status}")
    }
}
