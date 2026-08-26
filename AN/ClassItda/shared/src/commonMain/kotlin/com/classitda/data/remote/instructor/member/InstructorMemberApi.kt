package com.classitda.data.remote.instructor.member

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class InstructorMemberApi(
    private val client: HttpClient,
) {
    suspend fun getStudents(
        studioId: String,
        cursor: String?,
        size: Int,
    ): CursorResponseDto<StudioMembershipResponseDto> =
        client
            .get("api/studios/$studioId/memberships/students") {
                cursor?.let { parameter("cursor", it) }
                parameter("size", size)
            }.body()

    suspend fun enrollStudent(
        studioId: String,
        sessionId: String,
        membershipId: Long,
    ) {
        client
            .post("api/studios/$studioId/instructor/class-sessions/$sessionId/enrollments") {
                contentType(ContentType.Application.Json)
                setBody(InstructorEnrollmentCreateRequestDto(membershipId))
            }
    }

    suspend fun cancelEnrollment(
        studioId: String,
        sessionId: String,
        enrollmentId: String,
    ) {
        client.delete("api/studios/$studioId/instructor/class-sessions/$sessionId/enrollments/$enrollmentId")
    }
}
