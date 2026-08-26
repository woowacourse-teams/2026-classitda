package com.classitda.core.platform

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
import java.io.File
import java.util.UUID

@Composable
internal actual fun StudioImagePicker(
    source: StudioImageSource,
    onResult: (StudioImagePickerResult) -> Unit,
) {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
            val uri = pendingCameraUri
            pendingCameraUri = null
            if (uri == null) {
                currentOnResult(StudioImagePickerResult.Error(StudioImagePickerError.READ_FAILED))
                return@rememberLauncherForActivityResult
            }

            if (!captured) {
                deleteCameraUri(context, uri)
                currentOnResult(StudioImagePickerResult.Cancelled)
                return@rememberLauncherForActivityResult
            }

            val result =
                runCatching {
                    val selection = copyContentUriToCache(context, uri).getOrThrow()
                    markCameraUriReady(context, uri)
                    selection
                }
            deleteCameraUri(context, uri)
            currentOnResult(result.toPickerResult())
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val uri = pendingCameraUri
            if (!granted || uri == null) {
                pendingCameraUri = null
                uri?.let { deleteCameraUri(context, it) }
                currentOnResult(StudioImagePickerResult.Error(StudioImagePickerError.PERMISSION_DENIED))
            } else {
                cameraLauncher.launch(uri)
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
        val uri = createCameraUri(context)
        if (uri == null) {
            currentOnResult(StudioImagePickerResult.Error(StudioImagePickerError.READ_FAILED))
            return
        }
        pendingCameraUri = uri
        if (context.checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(uri)
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
            pendingCameraUri?.let { deleteCameraUri(context, it) }
            pendingCameraUri = null
        }
    }
}

internal actual fun releaseStudioImage(handle: String) {
    val file = File(handle)
    if (file.name.startsWith("classitda-studio-image-")) {
        file.delete()
    }
}

private fun createCameraUri(context: Context): Uri? {
    val values =
        ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "classitda-studio-${UUID.randomUUID()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/Classitda",
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
    return try {
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    } catch (_: SecurityException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }
}

private fun markCameraUriReady(
    context: Context,
    uri: Uri,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        context.contentResolver.update(
            uri,
            ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
            null,
            null,
        )
    }
}

private fun deleteCameraUri(
    context: Context,
    uri: Uri,
) {
    context.contentResolver.delete(uri, null, null)
}

private fun copyContentUriToCache(
    context: Context,
    uri: Uri,
): Result<StudioImagePickerSelection> =
    runCatching {
        val resolver = context.contentResolver
        val mimeType =
            resolver.getType(uri)
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
                    previewReference = file.absolutePath,
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
