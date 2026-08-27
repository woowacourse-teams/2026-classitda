package com.classitda.data.repository.instructor.membership

import com.classitda.data.remote.instructor.member.InstructorMemberApi
import com.classitda.data.remote.instructor.member.StudioMembershipDetailResponseDto
import com.classitda.data.remote.instructor.member.StudioMembershipResponseDto
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.ManagedMember
import com.classitda.domain.model.instructor.mypage.MemberListPage
import com.classitda.domain.model.instructor.mypage.MemberRegistrationDraft
import com.classitda.domain.model.studio.StudioId
import com.classitda.domain.repository.instructor.membership.InstructorMembershipRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.JsonConvertException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

internal class RemoteInstructorMembershipRepository(
    private val api: InstructorMemberApi,
) : InstructorMembershipRepository {
    override suspend fun getStudents(
        studioId: StudioId,
        cursor: String?,
        size: Int,
    ): InstructorMyPageResult<MemberListPage> =
        runRemoteQuery {
            require(size in 1..100) { "회원 목록 페이지 크기는 1부터 100까지여야 합니다." }
            api.getStudents(studioId.value, cursor, size).let { response ->
                if (response.hasNext && response.nextCursor == null) {
                    throw MembershipApiContractException("다음 페이지가 있지만 nextCursor가 없습니다.")
                }
                MemberListPage(
                    totalCount = response.items.size,
                    members = response.items.map(StudioMembershipResponseDto::toManagedMember),
                    nextPageCursor = response.nextCursor,
                )
            }
        }

    override suspend fun registerStudent(
        studioId: StudioId,
        draft: MemberRegistrationDraft,
    ): InstructorMyPageResult<Unit> =
        runRemoteQuery {
            api.registerStudent(studioId.value, draft.name, draft.phoneNumber)
        }

    override suspend fun getMembership(
        studioId: StudioId,
        membershipId: InstructorMemberId,
    ): InstructorMyPageResult<ManagedMember> =
        runRemoteQuery {
            api.getMembership(studioId.value, membershipId.toLongId()).toManagedMember()
        }

    override suspend fun updateStudent(
        studioId: StudioId,
        membershipId: InstructorMemberId,
        draft: MemberRegistrationDraft,
    ): InstructorMyPageResult<Unit> =
        runRemoteQuery {
            api.updateStudent(studioId.value, membershipId.toLongId(), draft.name, draft.phoneNumber)
        }

    override suspend fun deleteMembership(
        studioId: StudioId,
        membershipId: InstructorMemberId,
    ): InstructorMyPageResult<Unit> =
        runRemoteQuery {
            api.deleteMembership(studioId.value, membershipId.toLongId())
        }
}

private fun StudioMembershipResponseDto.toManagedMember() =
    ManagedMember(
        id = InstructorMemberId(id.toString()),
        name = name,
        phoneNumber = phoneNumber,
        registered = registered,
    )

private fun StudioMembershipDetailResponseDto.toManagedMember() =
    ManagedMember(
        id = InstructorMemberId(id.toString()),
        name = name,
        phoneNumber = phoneNumber,
        registered = registered,
    )

private fun InstructorMemberId.toLongId(): Long =
    value.toLongOrNull() ?: throw IllegalArgumentException("시설 소속 ID가 올바른 숫자가 아닙니다.")

private suspend inline fun <T> runRemoteQuery(block: suspend () -> T): InstructorMyPageResult<T> =
    try {
        InstructorMyPageResult.Success(block())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: ResponseException) {
        InstructorMyPageResult.Failure(exception.toMembershipFailureReason())
    } catch (exception: Throwable) {
        InstructorMyPageResult.Failure(exception.toMembershipFailureReason())
    }

private fun ResponseException.toMembershipFailureReason(): InstructorMyPageFailureReason =
    when (response.status.value) {
        400 -> InstructorMyPageFailureReason.INVALID_REQUEST
        401 -> InstructorMyPageFailureReason.UNAUTHORIZED
        403 -> InstructorMyPageFailureReason.FORBIDDEN
        404 -> InstructorMyPageFailureReason.NOT_FOUND
        409 -> InstructorMyPageFailureReason.CONFLICT
        in 500..599 -> InstructorMyPageFailureReason.SERVER
        else -> InstructorMyPageFailureReason.UNKNOWN
    }

private fun Throwable.toMembershipFailureReason(): InstructorMyPageFailureReason =
    when (this) {
        is MembershipApiContractException -> InstructorMyPageFailureReason.CONTRACT
        is IllegalArgumentException -> InstructorMyPageFailureReason.INVALID_REQUEST
        is JsonConvertException, is SerializationException -> InstructorMyPageFailureReason.CONTRACT
        is IOException -> InstructorMyPageFailureReason.NETWORK
        else -> InstructorMyPageFailureReason.UNKNOWN
    }

private class MembershipApiContractException(
    message: String,
) : IllegalStateException(message)
