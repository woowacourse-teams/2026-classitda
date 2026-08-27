package com.classitda.data.repository.instructor.mypage

import com.classitda.data.remote.instructor.mypage.studio.StudioRemoteDataSource
import com.classitda.data.remote.instructor.mypage.studio.StudioResponseDto
import com.classitda.data.remote.instructor.mypage.studio.StudioUpdateRequestDto
import com.classitda.data.remote.instructor.mypage.studio.toDomain
import com.classitda.data.remote.instructor.mypage.studio.toStudioCreateRequestDto
import com.classitda.data.remote.instructor.mypage.studio.toStudioUpdateRequestDto
import com.classitda.data.remote.instructor.mypage.studio.toWireId
import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.domain.model.instructor.mypage.ManagedStudio
import com.classitda.domain.model.instructor.mypage.StudioImageMutation
import com.classitda.domain.model.instructor.mypage.StudioImageSelection
import com.classitda.domain.model.instructor.mypage.StudioRegistrationDraft
import com.classitda.domain.model.instructor.mypage.UploadedStudioImage
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.domain.repository.instructor.mypage.InstructorStudioRepository
import com.classitda.domain.repository.instructor.mypage.StudioImageUploader
import com.classitda.domain.repository.instructor.mypage.StudioList
import com.classitda.domain.repository.instructor.mypage.StudioUpdateOperation
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

internal class RemoteInstructorStudioRepository(
    private val remoteDataSource: StudioRemoteDataSource,
    private val imageUploader: StudioImageUploader? = null,
) : InstructorStudioRepository {
    private var pendingCreate: PendingStudioCreate? = null
    private var pendingUpdate: PendingStudioUpdate? = null

    private suspend fun resolveCreateImage(draft: StudioRegistrationDraft): UploadedStudioImage? =
        when (val image = draft.image) {
            null -> {
                null
            }

            is StudioImageSelection.Remote -> {
                null
            }

            is StudioImageSelection.Local -> {
                pendingCreate
                    ?.takeIf { it.draft == draft }
                    ?.uploadedImage
                    ?: imageUploader?.upload(image)?.let { result ->
                        when (result) {
                            is InstructorMyPageResult.Success -> {
                                result.value
                            }

                            is InstructorMyPageResult.Failure -> {
                                throw StudioImageUploadFailure(result.reason)
                            }
                        }
                    }
                    ?: throw StudioImageUploadFailure(InstructorMyPageFailureReason.INVALID_REQUEST)
            }
        }

    override suspend fun getStudios(): InstructorMyPageResult<StudioList> =
        runRemoteQuery {
            when (val result = remoteDataSource.getMine().toDomain()) {
                is InstructorMyPageResult.Success -> {
                    InstructorMyPageResult.Success(
                        StudioList(
                            totalCount = result.value.size,
                            studios = result.value,
                        ),
                    )
                }

                is InstructorMyPageResult.Failure -> {
                    result
                }
            }
        }

    override suspend fun getStudio(studioId: InstructorStudioId): InstructorMyPageResult<ManagedStudio> {
        val wireStudioId =
            when (val result = studioId.toWireId()) {
                is InstructorMyPageResult.Success -> result.value
                is InstructorMyPageResult.Failure -> return result
            }

        return runRemoteQuery { remoteDataSource.get(wireStudioId).toDomain() }
    }

    override suspend fun registerStudio(draft: StudioRegistrationDraft): InstructorMyPageResult<Unit> =
        runRemoteQuery {
            val uploadedImage = resolveCreateImage(draft)
            val request =
                when (val result = draft.toStudioCreateRequestDto(uploadedImage)) {
                    is InstructorMyPageResult.Success -> result.value
                    is InstructorMyPageResult.Failure -> return@runRemoteQuery result
                }

            if (uploadedImage != null) {
                pendingCreate = PendingStudioCreate(draft, uploadedImage)
            }
            remoteDataSource.create(request)
            pendingCreate = null
            InstructorMyPageResult.Success(Unit)
        }

    override suspend fun updateStudio(
        studioId: InstructorStudioId,
        original: ManagedStudio,
        draft: StudioRegistrationDraft,
        imageMutation: StudioImageMutation,
    ): InstructorMyPageResult<Unit> {
        val wireStudioId =
            when (val result = studioId.toWireId()) {
                is InstructorMyPageResult.Success -> result.value
                is InstructorMyPageResult.Failure -> return result
            }

        val previous =
            pendingUpdate?.takeIf {
                it.studioId == studioId &&
                    it.original == original &&
                    it.imageMutation == imageMutation
            }
        val generalDraftChanged = previous != null && previous.draft.copy(image = null) != draft.copy(image = null)
        val completedOperations =
            if (generalDraftChanged) {
                previous.completedOperations - StudioUpdateOperation.PATCH
            } else {
                previous?.completedOperations.orEmpty()
            }
        pendingUpdate =
            PendingStudioUpdate(
                studioId = studioId,
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
            val shouldPatch = request.hasGeneralChanges() || imageMutation is StudioImageMutation.Replace
            if (shouldPatch && StudioUpdateOperation.PATCH !in currentPendingOperations()) {
                remoteDataSource.update(wireStudioId, request)
                markUpdateOperationCompleted(StudioUpdateOperation.PATCH)
            }
            if (imageMutation is StudioImageMutation.Remove &&
                StudioUpdateOperation.DELETE_IMAGE !in currentPendingOperations()
            ) {
                remoteDataSource.deleteImage(wireStudioId)
                markUpdateOperationCompleted(StudioUpdateOperation.DELETE_IMAGE)
            }
            pendingUpdate = null
            InstructorMyPageResult.Success(Unit)
        }
    }

    private suspend fun resolveUpdateImage(
        draft: StudioRegistrationDraft,
        imageMutation: StudioImageMutation,
    ): UploadedStudioImage? =
        when (imageMutation) {
            StudioImageMutation.Unchanged, StudioImageMutation.Remove -> {
                null
            }

            is StudioImageMutation.Replace -> {
                val localImage = imageMutation.image
                if (draft.image != localImage) {
                    throw StudioImageUploadFailure(InstructorMyPageFailureReason.INVALID_REQUEST)
                }
                pendingUpdate?.uploadedImage
                    ?: imageUploader?.upload(localImage)?.let { result ->
                        when (result) {
                            is InstructorMyPageResult.Success -> {
                                pendingUpdate = pendingUpdate?.copy(uploadedImage = result.value)
                                result.value
                            }

                            is InstructorMyPageResult.Failure -> {
                                throw StudioImageUploadFailure(result.reason)
                            }
                        }
                    }
                    ?: throw StudioImageUploadFailure(InstructorMyPageFailureReason.INVALID_REQUEST)
            }
        }

    private fun currentPendingOperations(): Set<StudioUpdateOperation> = pendingUpdate?.completedOperations.orEmpty()

    private fun markUpdateOperationCompleted(operation: StudioUpdateOperation) {
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
                reason = exception.toStudioFailureReason(),
                completedStudioUpdateOperations = currentPendingOperations(),
            )
        } catch (exception: Throwable) {
            InstructorMyPageResult.Failure(
                reason = exception.toStudioFailureReason(),
                completedStudioUpdateOperations = currentPendingOperations(),
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
        InstructorMyPageResult.Failure(exception.toStudioFailureReason())
    } catch (exception: Throwable) {
        InstructorMyPageResult.Failure(exception.toStudioFailureReason())
    }

private data class PendingStudioCreate(
    val draft: StudioRegistrationDraft,
    val uploadedImage: UploadedStudioImage,
)

private data class PendingStudioUpdate(
    val studioId: InstructorStudioId,
    val original: ManagedStudio,
    val draft: StudioRegistrationDraft,
    val imageMutation: StudioImageMutation,
    val uploadedImage: UploadedStudioImage?,
    val completedOperations: Set<StudioUpdateOperation>,
)

private class StudioImageUploadFailure(
    val reason: InstructorMyPageFailureReason,
) : IllegalStateException()

private fun List<StudioResponseDto>.toDomain(): InstructorMyPageResult<List<ManagedStudio>> {
    val studios = ArrayList<ManagedStudio>(size)
    for (response in this) {
        when (val result = response.toDomain()) {
            is InstructorMyPageResult.Success -> studios += result.value
            is InstructorMyPageResult.Failure -> return result
        }
    }
    return InstructorMyPageResult.Success(studios)
}

private suspend fun ResponseException.toStudioFailureReason(): InstructorMyPageFailureReason {
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

private fun Throwable.toStudioFailureReason(): InstructorMyPageFailureReason =
    when (this) {
        is JsonConvertException, is SerializationException -> {
            InstructorMyPageFailureReason.CONTRACT
        }

        is StudioImageUploadFailure -> {
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
        val jsonObject = studioErrorJson.parseToJsonElement(body).jsonObject
        jsonObject["code"]?.jsonPrimitive?.contentOrNull
            ?: jsonObject["errorCode"]?.jsonPrimitive?.contentOrNull
    }.getOrNull()

private val studioErrorJson = Json { ignoreUnknownKeys = true }

private fun StudioUpdateRequestDto.hasGeneralChanges(): Boolean =
    name != null ||
        address != null ||
        phoneNumber != null ||
        openTime != null ||
        closeTime != null ||
        description != null
