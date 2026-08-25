package com.classitda.data.repository.instructor.mypage

import com.classitda.data.remote.instructor.mypage.facility.StudioRemoteDataSource
import com.classitda.data.remote.instructor.mypage.facility.StudioResponseDto
import com.classitda.data.remote.instructor.mypage.facility.StudioUpdateRequestDto
import com.classitda.data.remote.instructor.mypage.facility.toDomain
import com.classitda.data.remote.instructor.mypage.facility.toStudioCreateRequestDto
import com.classitda.data.remote.instructor.mypage.facility.toStudioUpdateRequestDto
import com.classitda.data.remote.instructor.mypage.facility.toWireId
import com.classitda.domain.model.instructor.mypage.FacilityImageMutation
import com.classitda.domain.model.instructor.mypage.FacilityImageSelection
import com.classitda.domain.model.instructor.mypage.FacilityRegistrationDraft
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.ManagedFacility
import com.classitda.domain.model.instructor.mypage.UploadedFacilityImage
import com.classitda.domain.repository.instructor.mypage.FacilityImageUploader
import com.classitda.domain.repository.instructor.mypage.FacilityList
import com.classitda.domain.repository.instructor.mypage.FacilityUpdateOperation
import com.classitda.domain.repository.instructor.mypage.InstructorFacilityRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.JsonConvertException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class RemoteInstructorFacilityRepository(
    private val remoteDataSource: StudioRemoteDataSource,
    private val imageUploader: FacilityImageUploader? = null,
) : InstructorFacilityRepository {
    private var pendingCreate: PendingFacilityCreate? = null
    private var pendingUpdate: PendingFacilityUpdate? = null

    private suspend fun resolveCreateImage(draft: FacilityRegistrationDraft): UploadedFacilityImage? =
        when (val image = draft.image) {
            null -> {
                null
            }

            is FacilityImageSelection.Remote -> {
                null
            }

            is FacilityImageSelection.Local -> {
                pendingCreate
                    ?.takeIf { it.draft == draft }
                    ?.uploadedImage
                    ?: imageUploader?.upload(image)?.let { result ->
                        when (result) {
                            is InstructorMyPageResult.Success -> {
                                result.value
                            }

                            is InstructorMyPageResult.Failure -> {
                                throw FacilityImageUploadFailure(result.reason)
                            }
                        }
                    }
                    ?: throw FacilityImageUploadFailure(InstructorMyPageFailureReason.INVALID_REQUEST)
            }
        }

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
        runRemoteQuery {
            val uploadedImage = resolveCreateImage(draft)
            val request =
                when (val result = draft.toStudioCreateRequestDto(uploadedImage)) {
                    is InstructorMyPageResult.Success -> result.value
                    is InstructorMyPageResult.Failure -> return@runRemoteQuery result
                }

            if (uploadedImage != null) {
                pendingCreate = PendingFacilityCreate(draft, uploadedImage)
            }
            remoteDataSource.create(request)
            pendingCreate = null
            InstructorMyPageResult.Success(Unit)
        }

    override suspend fun updateFacility(
        facilityId: InstructorFacilityId,
        original: ManagedFacility,
        draft: FacilityRegistrationDraft,
        imageMutation: FacilityImageMutation,
    ): InstructorMyPageResult<Unit> {
        val studioId =
            when (val result = facilityId.toWireId()) {
                is InstructorMyPageResult.Success -> result.value
                is InstructorMyPageResult.Failure -> return result
            }

        val previous =
            pendingUpdate?.takeIf {
                it.facilityId == facilityId &&
                    it.original == original &&
                    it.imageMutation == imageMutation
            }
        val generalDraftChanged = previous != null && previous.draft.copy(image = null) != draft.copy(image = null)
        val completedOperations =
            if (generalDraftChanged) {
                previous.completedOperations - FacilityUpdateOperation.PATCH
            } else {
                previous?.completedOperations.orEmpty()
            }
        pendingUpdate =
            PendingFacilityUpdate(
                facilityId = facilityId,
                original = original,
                draft = draft,
                imageMutation = imageMutation,
                uploadedImage = previous?.uploadedImage,
                completedOperations = completedOperations,
            )

        return runUpdateQuery {
            val uploadedImage = resolveUpdateImage(draft, imageMutation)
            val request =
                when (val result = original.toStudioUpdateRequestDto(draft, imageMutation, uploadedImage)) {
                    is InstructorMyPageResult.Success -> result.value
                    is InstructorMyPageResult.Failure -> return@runUpdateQuery result
                }
            val shouldPatch = request.hasGeneralChanges() || imageMutation is FacilityImageMutation.Replace
            if (shouldPatch && FacilityUpdateOperation.PATCH !in currentPendingOperations()) {
                remoteDataSource.update(studioId, request)
                markUpdateOperationCompleted(FacilityUpdateOperation.PATCH)
            }
            if (imageMutation is FacilityImageMutation.Remove &&
                FacilityUpdateOperation.DELETE_IMAGE !in currentPendingOperations()
            ) {
                remoteDataSource.deleteImage(studioId)
                markUpdateOperationCompleted(FacilityUpdateOperation.DELETE_IMAGE)
            }
            pendingUpdate = null
            InstructorMyPageResult.Success(Unit)
        }
    }

    private suspend fun resolveUpdateImage(
        draft: FacilityRegistrationDraft,
        imageMutation: FacilityImageMutation,
    ): UploadedFacilityImage? =
        when (imageMutation) {
            FacilityImageMutation.Unchanged, FacilityImageMutation.Remove -> {
                null
            }

            is FacilityImageMutation.Replace -> {
                val localImage = imageMutation.image
                if (draft.image != localImage) {
                    throw FacilityImageUploadFailure(InstructorMyPageFailureReason.INVALID_REQUEST)
                }
                pendingUpdate?.uploadedImage
                    ?: imageUploader?.upload(localImage)?.let { result ->
                        when (result) {
                            is InstructorMyPageResult.Success -> {
                                pendingUpdate = pendingUpdate?.copy(uploadedImage = result.value)
                                result.value
                            }

                            is InstructorMyPageResult.Failure -> {
                                throw FacilityImageUploadFailure(result.reason)
                            }
                        }
                    }
                    ?: throw FacilityImageUploadFailure(InstructorMyPageFailureReason.INVALID_REQUEST)
            }
        }

    private fun currentPendingOperations(): Set<FacilityUpdateOperation> = pendingUpdate?.completedOperations.orEmpty()

    private fun markUpdateOperationCompleted(operation: FacilityUpdateOperation) {
        pendingUpdate =
            pendingUpdate?.copy(
                completedOperations = currentPendingOperations() + operation,
            )
    }

    private suspend inline fun <T> runUpdateQuery(
        block: suspend () -> InstructorMyPageResult<T>,
    ): InstructorMyPageResult<T> =
        try {
            block()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: ResponseException) {
            InstructorMyPageResult.Failure(
                reason = exception.toFacilityFailureReason(),
                completedFacilityUpdateOperations = currentPendingOperations(),
            )
        } catch (exception: Throwable) {
            InstructorMyPageResult.Failure(
                reason = exception.toFacilityFailureReason(),
                completedFacilityUpdateOperations = currentPendingOperations(),
            )
        }
}

private suspend inline fun <T> runRemoteQuery(
    block: suspend () -> InstructorMyPageResult<T>,
): InstructorMyPageResult<T> =
    try {
        block()
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: ResponseException) {
        InstructorMyPageResult.Failure(exception.toFacilityFailureReason())
    } catch (exception: Throwable) {
        InstructorMyPageResult.Failure(exception.toFacilityFailureReason())
    }

private data class PendingFacilityCreate(
    val draft: FacilityRegistrationDraft,
    val uploadedImage: UploadedFacilityImage,
)

private data class PendingFacilityUpdate(
    val facilityId: InstructorFacilityId,
    val original: ManagedFacility,
    val draft: FacilityRegistrationDraft,
    val imageMutation: FacilityImageMutation,
    val uploadedImage: UploadedFacilityImage?,
    val completedOperations: Set<FacilityUpdateOperation>,
)

private class FacilityImageUploadFailure(
    val reason: InstructorMyPageFailureReason,
) : IllegalStateException()

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

private suspend fun ResponseException.toFacilityFailureReason(): InstructorMyPageFailureReason {
    val code =
        runCatching { response.bodyAsText() }
            .getOrNull()
            ?.let(::errorCodeFrom)

    return when (code) {
        "STUDIO-008" -> {
            InstructorMyPageFailureReason.CONFLICT
        }

        "PERMISSION-001", "MEMBERSHIP-001" -> {
            InstructorMyPageFailureReason.FORBIDDEN
        }

        "STUDIO-002" -> {
            InstructorMyPageFailureReason.NOT_FOUND
        }

        "COMMON-001", "STUDIO-001", "STUDIO-007", "API-001" -> {
            InstructorMyPageFailureReason.INVALID_REQUEST
        }

        else -> {
            statusFailureReason(response.status.value)
        }
    }
}

private fun Throwable.toFacilityFailureReason(): InstructorMyPageFailureReason =
    when (this) {
        is JsonConvertException, is SerializationException -> {
            InstructorMyPageFailureReason.CONTRACT
        }

        is FacilityImageUploadFailure -> {
            reason
        }

        is IOException -> {
            InstructorMyPageFailureReason.NETWORK
        }

        else -> {
            InstructorMyPageFailureReason.UNKNOWN
        }
    }

private fun statusFailureReason(status: Int): InstructorMyPageFailureReason =
    when (status) {
        400 -> InstructorMyPageFailureReason.INVALID_REQUEST
        401 -> InstructorMyPageFailureReason.UNAUTHORIZED
        403 -> InstructorMyPageFailureReason.FORBIDDEN
        404 -> InstructorMyPageFailureReason.NOT_FOUND
        409 -> InstructorMyPageFailureReason.CONFLICT
        in 500..599 -> InstructorMyPageFailureReason.SERVER
        else -> InstructorMyPageFailureReason.UNKNOWN
    }

private fun errorCodeFrom(body: String): String? =
    runCatching {
        val jsonObject = facilityErrorJson.parseToJsonElement(body).jsonObject
        jsonObject["code"]?.jsonPrimitive?.contentOrNull
            ?: jsonObject["errorCode"]?.jsonPrimitive?.contentOrNull
    }.getOrNull()

private val facilityErrorJson = Json { ignoreUnknownKeys = true }

private fun StudioUpdateRequestDto.hasGeneralChanges(): Boolean =
    name != null ||
        address != null ||
        phoneNumber != null ||
        openTime != null ||
        closeTime != null ||
        description != null
