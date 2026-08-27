package com.classitda.data.remote.instructor.session

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.datetime.LocalDate

internal class InstructorSessionApi(
    private val client: HttpClient,
) {
    suspend fun getDailySessions(
        studioId: String,
        date: LocalDate,
    ): List<InstructorDailySessionResponseDto> =
        client
            .get("api/studios/$studioId/instructor/class-sessions/daily") {
                parameter("date", date.toString())
            }.body()

    suspend fun getCalendar(
        studioId: String,
        from: LocalDate,
        to: LocalDate,
    ): List<InstructorCalendarResponseDto> =
        client
            .get("api/studios/$studioId/instructor/class-sessions/calendar") {
                parameter("from", from.toString())
                parameter("to", to.toString())
            }.body()

    suspend fun getSession(
        studioId: String,
        sessionId: String,
    ): ClassSessionDetailResponseDto =
        client
            .get("api/studios/$studioId/instructor/class-sessions/$sessionId")
            .body()

    suspend fun updateSession(
        studioId: String,
        sessionId: String,
        request: ClassSessionUpdateV1RequestDto,
    ) {
        client.put("api/studios/$studioId/instructor/class-sessions/$sessionId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun cancelSession(
        studioId: String,
        sessionId: String,
    ) {
        client.delete("api/studios/$studioId/instructor/class-sessions/$sessionId")
    }
}
