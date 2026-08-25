package com.classitda.core.platform

import androidx.compose.runtime.Composable

internal enum class FacilityImageSource {
    CAMERA,
    GALLERY,
}

internal data class FacilityImagePickerSelection(
    val handle: String,
    val previewReference: String,
    val mimeType: String,
    val fileName: String,
    val sizeBytes: Long,
)

internal enum class FacilityImagePickerError {
    PERMISSION_DENIED,
    CAMERA_UNAVAILABLE,
    READ_FAILED,
    INVALID_MIME,
    FILE_TOO_LARGE,
    UNKNOWN,
}

internal sealed interface FacilityImagePickerResult {
    data class Selected(
        val selection: FacilityImagePickerSelection,
    ) : FacilityImagePickerResult

    data object Cancelled : FacilityImagePickerResult

    data class Error(
        val reason: FacilityImagePickerError,
    ) : FacilityImagePickerResult
}

/** Opens one native picker for the requested source and reports a platform-neutral selection. */
@Composable
internal expect fun FacilityImagePicker(
    source: FacilityImageSource,
    onResult: (FacilityImagePickerResult) -> Unit,
)

/** Removes a temporary local image created by the platform picker. */
internal expect fun releaseFacilityImage(handle: String)

internal const val FACILITY_IMAGE_MAX_SIZE_BYTES: Long = 5L * 1024L * 1024L

internal val FACILITY_IMAGE_ALLOWED_MIME_TYPES =
    setOf(
        "image/jpeg",
        "image/png",
        "image/webp",
    )

internal val FACILITY_IMAGE_ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")

internal fun validateFacilityImagePickerSelection(selection: FacilityImagePickerSelection): FacilityImagePickerError? =
    when {
        selection.mimeType !in FACILITY_IMAGE_ALLOWED_MIME_TYPES -> FacilityImagePickerError.INVALID_MIME

        selection.fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase() !in
            FACILITY_IMAGE_ALLOWED_EXTENSIONS -> FacilityImagePickerError.INVALID_MIME

        selection.sizeBytes > FACILITY_IMAGE_MAX_SIZE_BYTES -> FacilityImagePickerError.FILE_TOO_LARGE

        selection.sizeBytes <= 0L -> FacilityImagePickerError.READ_FAILED

        else -> null
    }
