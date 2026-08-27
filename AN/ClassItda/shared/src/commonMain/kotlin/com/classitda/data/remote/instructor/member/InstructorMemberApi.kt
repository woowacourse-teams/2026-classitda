package com.classitda.data.remote.instructor.member

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
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

    suspend fun registerStudent(
        studioId: String,
        name: String,
        phoneNumber: String,
    ) {
        client
            .post("api/studios/$studioId/memberships/students") {
                contentType(ContentType.Application.Json)
                setBody(StudentMembershipCreateRequestDto(name, phoneNumber))
            }
    }

    suspend fun getMembership(
        studioId: String,
        membershipId: Long,
    ): StudioMembershipDetailResponseDto =
        client
            .get("api/studios/$studioId/memberships/$membershipId")
            .body()

    suspend fun updateStudent(
        studioId: String,
        membershipId: Long,
        name: String,
        phoneNumber: String,
    ) {
        client
            .patch("api/studios/$studioId/memberships/$membershipId") {
                contentType(ContentType.Application.Json)
                setBody(StudentMembershipUpdateRequestDto(name, phoneNumber))
            }
    }

    suspend fun deleteMembership(
        studioId: String,
        membershipId: Long,
    ) {
        client.delete("api/studios/$studioId/memberships/$membershipId")
    }

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
