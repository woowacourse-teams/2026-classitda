package com.classitda.core.platform

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

@Composable
internal actual fun StudioImagePicker(
    source: StudioImageSource,
    onResult: (StudioImagePickerResult) -> Unit,
) {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)
    var pendingCameraCapture by remember { mutableStateOf<CameraCapture?>(null) }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
            val capture = pendingCameraCapture
            pendingCameraCapture = null
            if (capture == null) {
                currentOnResult(StudioImagePickerResult.Error(StudioImagePickerError.READ_FAILED))
                return@rememberLauncherForActivityResult
            }

            if (!captured) {
                capture.file.delete()
                currentOnResult(StudioImagePickerResult.Cancelled)
                return@rememberLauncherForActivityResult
            }

            val result =
                runCatching {
                    copyContentUriToCache(context, capture.uri, fallbackMimeType = "image/jpeg").getOrThrow()
                }
            capture.file.delete()
            currentOnResult(result.toPickerResult())
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val capture = pendingCameraCapture
            if (!granted || capture == null) {
                pendingCameraCapture = null
                capture?.file?.delete()
                currentOnResult(StudioImagePickerResult.Error(StudioImagePickerError.PERMISSION_DENIED))
            } else {
                cameraLauncher.launch(capture.uri)
            }
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) {
                currentOnResult(StudioImagePickerResult.Cancelled)
            } else {
                currentOnResult(copyContentUriToCache(context, uri).toPickerResult())
            }
        }

    fun startCamera() {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            currentOnResult(StudioImagePickerResult.Error(StudioImagePickerError.CAMERA_UNAVAILABLE))
            return
        }
        val capture = createCameraCapture(context)
        if (capture == null) {
            currentOnResult(StudioImagePickerResult.Error(StudioImagePickerError.READ_FAILED))
            return
        }
        pendingCameraCapture = capture
        if (context.checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(capture.uri)
        } else {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(source) {
        when (source) {
            StudioImageSource.CAMERA -> {
                startCamera()
            }

            StudioImageSource.GALLERY -> {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            pendingCameraCapture?.file?.delete()
            pendingCameraCapture = null
        }
    }
}

private data class CameraCapture(
    val file: File,
    val uri: Uri,
)

internal actual fun releaseStudioImage(handle: String) {
    val file = File(handle)
    if (file.name.startsWith("classitda-studio-image-")) {
        file.delete()
    }
}

private fun createCameraCapture(context: Context): CameraCapture? =
    runCatching {
        val file = File.createTempFile("classitda-camera-", ".jpg", context.cacheDir)
        val authority = "${context.packageName}.fileprovider"
        CameraCapture(
            file = file,
            uri = FileProvider.getUriForFile(context, authority, file),
        )
    }.getOrNull()

private fun copyContentUriToCache(
    context: Context,
    uri: Uri,
    fallbackMimeType: String? = null,
): Result<StudioImagePickerSelection> =
    runCatching {
        val resolver = context.contentResolver
        val mimeType =
            resolver.getType(uri)
                ?: fallbackMimeType
                ?: throw StudioImagePickerException(StudioImagePickerError.INVALID_MIME)
        if (mimeType !in STUDIO_IMAGE_ALLOWED_MIME_TYPES) {
            throw StudioImagePickerException(StudioImagePickerError.INVALID_MIME)
        }
        val sourceSize = querySize(resolver, uri)
        if (sourceSize > STUDIO_IMAGE_MAX_SIZE_BYTES) {
            throw StudioImagePickerException(StudioImagePickerError.FILE_TOO_LARGE)
        }
        val extension = mimeType.substringAfter('/', "jpg")
        val file = File(context.cacheDir, "classitda-studio-image-${UUID.randomUUID()}.$extension")
        try {
            resolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            } ?: throw StudioImagePickerException(StudioImagePickerError.READ_FAILED)
            val sizeBytes = file.length()
            if (sizeBytes <= 0L) {
                throw StudioImagePickerException(StudioImagePickerError.READ_FAILED)
            }
            if (sizeBytes > STUDIO_IMAGE_MAX_SIZE_BYTES) {
                throw StudioImagePickerException(StudioImagePickerError.FILE_TOO_LARGE)
            }
            val selection =
                StudioImagePickerSelection(
                    handle = file.absolutePath,
                    previewReference = Uri.fromFile(file).toString(),
                    mimeType = mimeType,
                    fileName = file.name,
                    sizeBytes = sizeBytes,
                )
            validateStudioImagePickerSelection(selection)?.let(::throwPickerError)
            selection
        } catch (error: Throwable) {
            file.delete()
            throw error
        }
    }

private fun querySize(
    resolver: android.content.ContentResolver,
    uri: Uri,
): Long {
    resolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (index >= 0 && !cursor.isNull(index)) return cursor.getLong(index)
        }
    }
    return resolver.openAssetFileDescriptor(uri, "r")?.use { it.length }?.takeIf { it >= 0L } ?: 0L
}

private fun Result<StudioImagePickerSelection>.toPickerResult(): StudioImagePickerResult =
    fold(
        onSuccess = { StudioImagePickerResult.Selected(it) },
        onFailure = { error ->
            StudioImagePickerResult.Error(
                (error as? StudioImagePickerException)?.reason ?: StudioImagePickerError.READ_FAILED,
            )
        },
    )

private class StudioImagePickerException(
    val reason: StudioImagePickerError,
) : IllegalStateException()

private fun throwPickerError(reason: StudioImagePickerError): Nothing = throw StudioImagePickerException(reason)
