package com.classitda.data.repository.instructor.mypage

import com.classitda.data.remote.member.MemberApi
import com.classitda.data.remote.member.MemberMeResponseDto
import com.classitda.domain.model.instructor.mypage.InstructorAccountProfile
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.domain.repository.instructor.mypage.InstructorProfileRepository
import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.JsonConvertException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

internal class RemoteInstructorProfileRepository(
    private val api: MemberApi,
) : InstructorProfileRepository {
    override suspend fun getProfile(): InstructorMyPageResult<InstructorAccountProfile> =
        runRemoteQuery {
            api.getMe().toDomain()
        }

    override suspend fun updateProfileName(name: String): InstructorMyPageResult<InstructorAccountProfile> =
        runRemoteQuery {
            api.updateName(name)
            api.getMe().toDomain()
        }
}

private fun MemberMeResponseDto.toDomain() =
    InstructorAccountProfile(
        name = name,
        phoneNumber = phoneNumber,
        email = email,
    )

private suspend inline fun <T> runRemoteQuery(
    block: suspend () -> T,
): InstructorMyPageResult<T> =
    try {
        InstructorMyPageResult.Success(block())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: ResponseException) {
        InstructorMyPageResult.Failure(exception.toProfileFailureReason())
    } catch (exception: Throwable) {
        InstructorMyPageResult.Failure(exception.toProfileFailureReason())
    }

private fun ResponseException.toProfileFailureReason(): InstructorMyPageFailureReason =
    when (response.status.value) {
        400 -> InstructorMyPageFailureReason.INVALID_REQUEST
        401 -> InstructorMyPageFailureReason.UNAUTHORIZED
        403 -> InstructorMyPageFailureReason.FORBIDDEN
        404 -> InstructorMyPageFailureReason.NOT_FOUND
        in 500..599 -> InstructorMyPageFailureReason.SERVER
        else -> InstructorMyPageFailureReason.UNKNOWN
    }

private fun Throwable.toProfileFailureReason(): InstructorMyPageFailureReason =
    when (this) {
        is JsonConvertException, is SerializationException -> InstructorMyPageFailureReason.CONTRACT
        is IOException -> InstructorMyPageFailureReason.NETWORK
        else -> InstructorMyPageFailureReason.UNKNOWN
    }
