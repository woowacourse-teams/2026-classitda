package com.classitda.data.remote.api

import co.touchlab.kermit.Logger
import com.classitda.data.remote.dto.ClassSessionCreateRequestDto
import com.classitda.data.remote.dto.ClassSessionUpdateRequestDto
import com.classitda.data.remote.dto.InstructorDailySessionResponseDto
import com.classitda.data.remote.instructor.member.CursorResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class ClassSessionsApi(
    private val client: HttpClient,
) {
    suspend fun getInstructorClassSessions(
        studioId: Long,
        cursor: String?,
        size: Int,
    ): CursorResponseDto<InstructorDailySessionResponseDto> {
        val response: CursorResponseDto<InstructorDailySessionResponseDto> =
            client
                .get("api/studios/$studioId/instructor/class-sessions") {
                    cursor?.let { parameter("cursor", it) }
                    parameter("size", size)
                }.body()
        Logger.d("getInstructorClassSessions response: $response")
        return response
    }

    // 201 Created만 내려주고 본문은 없다. body()로 역직렬화하면 안 된다.
    suspend fun createClassSession(
        studioId: Long,
        request: ClassSessionCreateRequestDto,
    ) {
        val response =
            client.post("api/studios/$studioId/instructor/class-sessions") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        Logger.d("createClassSession response: ${response.status}")
    }

    suspend fun updateClassSession(
        studioId: Long,
        classSessionId: Long,
        request: ClassSessionUpdateRequestDto,
    ) {
        val response =
            client.put("api/studios/$studioId/instructor/class-sessions/$classSessionId") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        Logger.d("updateClassSession response: ${response.status}")
    }

    suspend fun cancelClassSession(
        studioId: Long,
        classSessionId: Long,
    ) {
        val response = client.delete("api/studios/$studioId/instructor/class-sessions/$classSessionId")
        Logger.d("cancelClassSession response: ${response.status}")
    }
}
