package com.classitda.data.repository.instructor.mypage

import com.classitda.core.platform.FACILITY_IMAGE_ALLOWED_EXTENSIONS
import com.classitda.core.platform.FACILITY_IMAGE_ALLOWED_MIME_TYPES
import com.classitda.core.platform.FACILITY_IMAGE_MAX_SIZE_BYTES
import com.classitda.core.platform.releaseFacilityImage
import com.classitda.data.remote.instructor.mypage.facility.FacilityImageUploadApi
import com.classitda.data.remote.instructor.mypage.facility.ImageUploadUrlRequestDto
import com.classitda.data.remote.instructor.mypage.facility.ObjectStorageUploadDataSource
import com.classitda.data.remote.instructor.mypage.facility.ObjectStorageUploadResult
import com.classitda.domain.model.instructor.mypage.FacilityImageSelection
import com.classitda.domain.model.instructor.mypage.UploadedFacilityImage
import com.classitda.domain.repository.instructor.mypage.FacilityImageUploader
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import io.ktor.client.plugins.ResponseException
import io.ktor.http.ContentType
import io.ktor.serialization.JsonConvertException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

internal class RemoteFacilityImageUploader(
    private val uploadApi: FacilityImageUploadApi,
    private val objectStorage: ObjectStorageUploadDataSource,
    private val releaseLocalImage: (String) -> Unit = ::releaseFacilityImage,
) : FacilityImageUploader {
    override suspend fun upload(image: FacilityImageSelection.Local): InstructorMyPageResult<UploadedFacilityImage> {
        val metadata =
            when (val validation = image.validateForUpload()) {
                is ImageUploadValidation.Valid -> validation.metadata
                is ImageUploadValidation.Invalid -> return InstructorMyPageResult.Failure(validation.reason)
            }

        return try {
            for (attempt in 0 until MAX_UPLOAD_ATTEMPTS) {
                val uploadInfo = uploadApi.issueUrl(ImageUploadUrlRequestDto(metadata.extension, metadata.sizeBytes))
                val objectKey = uploadInfo.objectKey?.takeIf(String::isNotBlank) ?: return contractFailure()
                val uploadUrl = uploadInfo.uploadUrl?.takeIf(String::isNotBlank) ?: return contractFailure()
                val contentType = uploadInfo.contentType?.takeIf(String::isNotBlank) ?: return contractFailure()
                if (!uploadUrl.isAbsoluteHttpUrl()) return contractFailure()
                if (runCatching { ContentType.parse(contentType) }.isFailure) return contractFailure()

                when (
                    val result =
                        objectStorage.put(
                            uploadUrl = uploadUrl,
                            contentType = contentType,
                            sizeBytes = metadata.sizeBytes,
                            image = image,
                        )
                ) {
                    ObjectStorageUploadResult.Accepted -> {
                        releaseLocalImage(image.handle)
                        return InstructorMyPageResult.Success(UploadedFacilityImage(objectKey))
                    }

                    ObjectStorageUploadResult.ReadFailed -> {
                        return InstructorMyPageResult.Failure(InstructorMyPageFailureReason.IMAGE_READ_FAILED)
                    }

                    ObjectStorageUploadResult.NetworkFailure -> {
                        return InstructorMyPageResult.Failure(InstructorMyPageFailureReason.NETWORK)
                    }

                    ObjectStorageUploadResult.Rejected -> {
                        if (attempt == MAX_UPLOAD_ATTEMPTS - 1) {
                            return InstructorMyPageResult.Failure(
                                InstructorMyPageFailureReason.UPLOAD_EXPIRED_OR_REJECTED,
                            )
                        }
                    }
                }
            }
            InstructorMyPageResult.Failure(InstructorMyPageFailureReason.UPLOAD_EXPIRED_OR_REJECTED)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Throwable) {
            InstructorMyPageResult.Failure(exception.toImageUploadFailureReason())
        }
    }
}

private data class ImageUploadMetadata(
    val extension: String,
    val sizeBytes: Long,
)

private sealed interface ImageUploadValidation {
    data class Valid(
        val metadata: ImageUploadMetadata,
    ) : ImageUploadValidation

    data class Invalid(
        val reason: InstructorMyPageFailureReason,
    ) : ImageUploadValidation
}

private fun FacilityImageSelection.Local.validateForUpload(): ImageUploadValidation {
    val extension = fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
    if (sizeBytes <= 0L) {
        return ImageUploadValidation.Invalid(InstructorMyPageFailureReason.IMAGE_READ_FAILED)
    }
    if (sizeBytes > FACILITY_IMAGE_MAX_SIZE_BYTES) {
        return ImageUploadValidation.Invalid(InstructorMyPageFailureReason.IMAGE_TOO_LARGE)
    }
    if (extension !in FACILITY_IMAGE_ALLOWED_EXTENSIONS || mimeType !in FACILITY_IMAGE_ALLOWED_MIME_TYPES) {
        return ImageUploadValidation.Invalid(InstructorMyPageFailureReason.UNSUPPORTED_IMAGE)
    }
    if (!mimeType.matchesExtension(extension)) {
        return ImageUploadValidation.Invalid(InstructorMyPageFailureReason.UNSUPPORTED_IMAGE)
    }
    return ImageUploadValidation.Valid(ImageUploadMetadata(extension, sizeBytes))
}

private fun String.matchesExtension(extension: String): Boolean =
    when (this) {
        "image/jpeg" -> extension == "jpg" || extension == "jpeg"
        "image/png" -> extension == "png"
        "image/webp" -> extension == "webp"
        else -> false
    }

private fun String.isAbsoluteHttpUrl(): Boolean = startsWith("https://") || startsWith("http://")

private fun contractFailure(): InstructorMyPageResult.Failure =
    InstructorMyPageResult.Failure(InstructorMyPageFailureReason.CONTRACT)

private fun Throwable.toImageUploadFailureReason(): InstructorMyPageFailureReason =
    when (this) {
        is ResponseException -> {
            when (response.status.value) {
                400 -> InstructorMyPageFailureReason.INVALID_REQUEST
                401 -> InstructorMyPageFailureReason.UNAUTHORIZED
                403 -> InstructorMyPageFailureReason.FORBIDDEN
                404 -> InstructorMyPageFailureReason.NOT_FOUND
                409 -> InstructorMyPageFailureReason.CONFLICT
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

private const val MAX_UPLOAD_ATTEMPTS = 2
