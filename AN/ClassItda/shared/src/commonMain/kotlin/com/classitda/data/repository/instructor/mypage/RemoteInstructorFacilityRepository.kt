package com.classitda.data.repository.instructor.mypage

import com.classitda.data.remote.instructor.mypage.facility.StudioRemoteDataSource
import com.classitda.data.remote.instructor.mypage.facility.StudioResponseDto
import com.classitda.data.remote.instructor.mypage.facility.toDomain
import com.classitda.data.remote.instructor.mypage.facility.toWireId
import com.classitda.domain.model.instructor.mypage.FacilityRegistrationDraft
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.ManagedFacility
import com.classitda.domain.repository.instructor.mypage.FacilityList
import com.classitda.domain.repository.instructor.mypage.InstructorFacilityRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.JsonConvertException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

internal class RemoteInstructorFacilityRepository(
    private val remoteDataSource: StudioRemoteDataSource,
) : InstructorFacilityRepository {
    override suspend fun getFacilities(): InstructorMyPageResult<FacilityList> =
        runRemoteQuery {
            when (val result = remoteDataSource.getMine().toDomain()) {
                is InstructorMyPageResult.Success -> {
                    InstructorMyPageResult.Success(
                        FacilityList(
                            totalCount = result.value.size,
                            facilities = result.value,
                        ),
                    )
                }

                is InstructorMyPageResult.Failure -> {
                    result
                }
            }
        }

    override suspend fun getFacility(facilityId: InstructorFacilityId): InstructorMyPageResult<ManagedFacility> {
        val studioId =
            when (val result = facilityId.toWireId()) {
                is InstructorMyPageResult.Success -> result.value
                is InstructorMyPageResult.Failure -> return result
            }

        return runRemoteQuery { remoteDataSource.get(studioId).toDomain() }
    }

    override suspend fun registerFacility(draft: FacilityRegistrationDraft): InstructorMyPageResult<Unit> =
        throw UnsupportedOperationException("시설 생성 API는 P6에서 연결합니다.")

    override suspend fun updateFacility(
        facilityId: InstructorFacilityId,
        draft: FacilityRegistrationDraft,
    ): InstructorMyPageResult<Unit> = throw UnsupportedOperationException("시설 수정 API는 P7에서 연결합니다.")
}

private suspend inline fun <T> runRemoteQuery(
    block: suspend () -> InstructorMyPageResult<T>,
): InstructorMyPageResult<T> =
    try {
        block()
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Throwable) {
        InstructorMyPageResult.Failure(exception.toFacilityFailureReason())
    }

private fun List<StudioResponseDto>.toDomain(): InstructorMyPageResult<List<ManagedFacility>> {
    val facilities = ArrayList<ManagedFacility>(size)
    for (response in this) {
        when (val result = response.toDomain()) {
            is InstructorMyPageResult.Success -> facilities += result.value
            is InstructorMyPageResult.Failure -> return result
        }
    }
    return InstructorMyPageResult.Success(facilities)
}

private fun Throwable.toFacilityFailureReason(): InstructorMyPageFailureReason =
    when (this) {
        is ResponseException -> {
            when (response.status.value) {
                400 -> InstructorMyPageFailureReason.INVALID_REQUEST
                401 -> InstructorMyPageFailureReason.UNAUTHORIZED
                403 -> InstructorMyPageFailureReason.FORBIDDEN
                404 -> InstructorMyPageFailureReason.NOT_FOUND
                in 500..599 -> InstructorMyPageFailureReason.SERVER
                else -> InstructorMyPageFailureReason.UNKNOWN
            }
        }

        is JsonConvertException, is SerializationException -> {
            InstructorMyPageFailureReason.CONTRACT
        }

        is IOException -> {
            InstructorMyPageFailureReason.NETWORK
        }

        else -> {
            InstructorMyPageFailureReason.UNKNOWN
        }
    }
