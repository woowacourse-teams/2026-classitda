package com.classitda.core.platform

import androidx.compose.runtime.Composable

internal enum class StudioImageSource {
    CAMERA,
    GALLERY,
}

internal data class StudioImagePickerSelection(
    val handle: String,
    val previewReference: String,
    val mimeType: String,
    val fileName: String,
    val sizeBytes: Long,
)

internal enum class StudioImagePickerError {
    PERMISSION_DENIED,
    CAMERA_UNAVAILABLE,
    READ_FAILED,
    INVALID_MIME,
    FILE_TOO_LARGE,
    UNKNOWN,
}

internal sealed interface StudioImagePickerResult {
    data class Selected(
        val selection: StudioImagePickerSelection,
    ) : StudioImagePickerResult

    data object Cancelled : StudioImagePickerResult

    data class Error(
        val reason: StudioImagePickerError,
    ) : StudioImagePickerResult
}

/** Opens one native picker for the requested source and reports a platform-neutral selection. */
@Composable
internal expect fun StudioImagePicker(
    source: StudioImageSource,
    onResult: (StudioImagePickerResult) -> Unit,
)

/** Removes a temporary local image created by the platform picker. */
internal expect fun releaseStudioImage(handle: String)

internal const val STUDIO_IMAGE_MAX_SIZE_BYTES: Long = 5L * 1024L * 1024L

internal val STUDIO_IMAGE_ALLOWED_MIME_TYPES =
    setOf(
        "image/jpeg",
        "image/png",
        "image/webp",
    )

internal val STUDIO_IMAGE_ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")

internal fun validateStudioImagePickerSelection(selection: StudioImagePickerSelection): StudioImagePickerError? =
    when {
        selection.mimeType !in STUDIO_IMAGE_ALLOWED_MIME_TYPES -> StudioImagePickerError.INVALID_MIME

        selection.fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase() !in
            STUDIO_IMAGE_ALLOWED_EXTENSIONS -> StudioImagePickerError.INVALID_MIME

        selection.sizeBytes > STUDIO_IMAGE_MAX_SIZE_BYTES -> StudioImagePickerError.FILE_TOO_LARGE

        selection.sizeBytes <= 0L -> StudioImagePickerError.READ_FAILED

        else -> null
    }
