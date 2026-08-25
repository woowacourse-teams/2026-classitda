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
internal actual fun FacilityImagePicker(
    source: FacilityImageSource,
    onResult: (FacilityImagePickerResult) -> Unit,
) {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { captured ->
            val uri = pendingCameraUri
            pendingCameraUri = null
            if (uri == null) {
                currentOnResult(FacilityImagePickerResult.Error(FacilityImagePickerError.READ_FAILED))
                return@rememberLauncherForActivityResult
            }

            if (!captured) {
                deleteCameraUri(context, uri)
                currentOnResult(FacilityImagePickerResult.Cancelled)
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
                currentOnResult(FacilityImagePickerResult.Error(FacilityImagePickerError.PERMISSION_DENIED))
            } else {
                cameraLauncher.launch(uri)
            }
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) {
                currentOnResult(FacilityImagePickerResult.Cancelled)
            } else {
                currentOnResult(copyContentUriToCache(context, uri).toPickerResult())
            }
        }

    fun startCamera() {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            currentOnResult(FacilityImagePickerResult.Error(FacilityImagePickerError.CAMERA_UNAVAILABLE))
            return
        }
        val uri = createCameraUri(context)
        if (uri == null) {
            currentOnResult(FacilityImagePickerResult.Error(FacilityImagePickerError.READ_FAILED))
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
            FacilityImageSource.CAMERA -> {
                startCamera()
            }

            FacilityImageSource.GALLERY -> {
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

internal actual fun releaseFacilityImage(handle: String) {
    val file = File(handle)
    if (file.name.startsWith("classitda-facility-image-")) {
        file.delete()
    }
}

private fun createCameraUri(context: Context): Uri? {
    val values =
        ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "classitda-facility-${UUID.randomUUID()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/Classitda",
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
    return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
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
): Result<FacilityImagePickerSelection> =
    runCatching {
        val resolver = context.contentResolver
        val mimeType =
            resolver.getType(uri)
                ?: throw FacilityImagePickerException(FacilityImagePickerError.INVALID_MIME)
        if (mimeType !in FACILITY_IMAGE_ALLOWED_MIME_TYPES) {
            throw FacilityImagePickerException(FacilityImagePickerError.INVALID_MIME)
        }
        val sourceSize = querySize(resolver, uri)
        if (sourceSize > FACILITY_IMAGE_MAX_SIZE_BYTES) {
            throw FacilityImagePickerException(FacilityImagePickerError.FILE_TOO_LARGE)
        }
        val extension = mimeType.substringAfter('/', "jpg")
        val file = File(context.cacheDir, "classitda-facility-image-${UUID.randomUUID()}.$extension")
        try {
            resolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            } ?: throw FacilityImagePickerException(FacilityImagePickerError.READ_FAILED)
            val sizeBytes = file.length()
            if (sizeBytes <= 0L) {
                throw FacilityImagePickerException(FacilityImagePickerError.READ_FAILED)
            }
            if (sizeBytes > FACILITY_IMAGE_MAX_SIZE_BYTES) {
                throw FacilityImagePickerException(FacilityImagePickerError.FILE_TOO_LARGE)
            }
            val selection =
                FacilityImagePickerSelection(
                    handle = file.absolutePath,
                    previewReference = file.absolutePath,
                    mimeType = mimeType,
                    fileName = queryDisplayName(resolver, uri) ?: file.name,
                    sizeBytes = sizeBytes,
                )
            validateFacilityImagePickerSelection(selection)?.let(::throwPickerError)
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

private fun queryDisplayName(
    resolver: android.content.ContentResolver,
    uri: Uri,
): String? =
    resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0) cursor.getString(index) else null
        } else {
            null
        }
    }

private fun Result<FacilityImagePickerSelection>.toPickerResult(): FacilityImagePickerResult =
    fold(
        onSuccess = { FacilityImagePickerResult.Selected(it) },
        onFailure = { error ->
            FacilityImagePickerResult.Error(
                (error as? FacilityImagePickerException)?.reason ?: FacilityImagePickerError.READ_FAILED,
            )
        },
    )

private class FacilityImagePickerException(
    val reason: FacilityImagePickerError,
) : IllegalStateException()

private fun throwPickerError(reason: FacilityImagePickerError): Nothing = throw FacilityImagePickerException(reason)
