package com.classitda.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSNumber
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSURLFileSizeKey
import platform.Foundation.NSUUID
import platform.Foundation.writeToFile
import platform.Photos.PHPhotoLibrary
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun StudioImagePicker(
    source: StudioImageSource,
    onResult: (StudioImagePickerResult) -> Unit,
) {
    val hostController = LocalUIViewController.current
    val currentOnResult by rememberUpdatedState(onResult)
    val delegate =
        remember(source) {
            IosStudioImagePickerDelegate { result ->
                currentOnResult(result)
            }
        }

    LaunchedEffect(source, delegate) {
        when (source) {
            StudioImageSource.CAMERA -> {
                presentCamera(hostController, delegate)
            }

            StudioImageSource.GALLERY -> {
                presentGallery(hostController, delegate)
            }
        }
    }

    DisposableEffect(delegate) {
        onDispose {
            delegate.dismissPresentedController()
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun releaseStudioImage(handle: String) {
    if (handle.contains("classitda-studio-image-")) {
        NSFileManager.defaultManager.removeItemAtPath(handle, error = null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun presentCamera(
    hostController: UIViewController,
    delegate: IosStudioImagePickerDelegate,
) {
    val cameraType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
    if (!UIImagePickerController.isSourceTypeAvailable(cameraType)) {
        delegate.report(StudioImagePickerResult.Error(StudioImagePickerError.CAMERA_UNAVAILABLE))
        return
    }

    when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
        AVAuthorizationStatusDenied,
        AVAuthorizationStatusRestricted,
        -> {
            delegate.report(StudioImagePickerResult.Error(StudioImagePickerError.PERMISSION_DENIED))
        }

        AVAuthorizationStatusAuthorized -> {
            showCamera(hostController, delegate, cameraType)
        }

        AVAuthorizationStatusNotDetermined -> {
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted: Boolean ->
                if (granted) {
                    showCamera(hostController, delegate, cameraType)
                } else {
                    delegate.report(StudioImagePickerResult.Error(StudioImagePickerError.PERMISSION_DENIED))
                }
            }
        }

        else -> {
            delegate.report(StudioImagePickerResult.Error(StudioImagePickerError.UNKNOWN))
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun showCamera(
    hostController: UIViewController,
    delegate: IosStudioImagePickerDelegate,
    cameraType: UIImagePickerControllerSourceType,
) {
    val picker =
        UIImagePickerController().apply {
            sourceType = cameraType
            mediaTypes = listOf("public.image")
            allowsEditing = false
            this.delegate = delegate
        }
    delegate.presentedController = picker
    hostController.presentViewController(picker, animated = true, completion = null)
}

@OptIn(ExperimentalForeignApi::class)
private fun presentGallery(
    hostController: UIViewController,
    delegate: IosStudioImagePickerDelegate,
) {
    val configuration =
        PHPickerConfiguration(photoLibrary = PHPhotoLibrary.sharedPhotoLibrary()).apply {
            selectionLimit = 1
            filter = PHPickerFilter.imagesFilter
        }
    val picker = PHPickerViewController(configuration = configuration).apply { this.delegate = delegate }
    delegate.presentedController = picker
    hostController.presentViewController(picker, animated = true, completion = null)
}

@OptIn(ExperimentalForeignApi::class)
private class IosStudioImagePickerDelegate(
    private val onResult: (StudioImagePickerResult) -> Unit,
) : NSObject(),
    UIImagePickerControllerDelegateProtocol,
    UINavigationControllerDelegateProtocol,
    PHPickerViewControllerDelegateProtocol {
    var presentedController: UIViewController? = null

    fun report(result: StudioImagePickerResult) {
        onResult(result)
    }

    fun dismissPresentedController() {
        presentedController?.dismissViewControllerAnimated(true, completion = null)
        presentedController = null
    }

    @ObjCSignatureOverride
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image =
            didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage]
                as? UIImage
        dismissPresentedController()
        if (image == null) {
            onResult(StudioImagePickerResult.Error(StudioImagePickerError.READ_FAILED))
        } else {
            onResult(writeCameraImage(image))
        }
    }

    @ObjCSignatureOverride
    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        dismissPresentedController()
        onResult(StudioImagePickerResult.Cancelled)
    }

    @ObjCSignatureOverride
    override fun picker(
        picker: PHPickerViewController,
        didFinishPicking: List<*>,
    ) {
        dismissPresentedController()
        val result = didFinishPicking.firstOrNull() as? platform.PhotosUI.PHPickerResult
        if (result == null) {
            onResult(StudioImagePickerResult.Cancelled)
            return
        }
        result.itemProvider.loadFileRepresentationForTypeIdentifier("public.image") { url, _ ->
            if (url == null) {
                onResult(StudioImagePickerResult.Error(StudioImagePickerError.READ_FAILED))
            } else {
                onResult(copyGalleryImage(url))
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun writeCameraImage(image: UIImage): StudioImagePickerResult {
    val data =
        UIImageJPEGRepresentation(image, 0.9)
            ?: return StudioImagePickerResult.Error(StudioImagePickerError.READ_FAILED)
    return writeImageData(data, "jpg", "image/jpeg", "studio-camera.jpg")
}

@OptIn(ExperimentalForeignApi::class)
private fun copyGalleryImage(url: NSURL): StudioImagePickerResult {
    val extension = url.pathExtension?.lowercase() ?: "jpg"
    val mimeType =
        extension.toImageMimeType()
            ?: return StudioImagePickerResult.Error(StudioImagePickerError.INVALID_MIME)
    val sourcePath =
        url.path ?: return StudioImagePickerResult.Error(StudioImagePickerError.READ_FAILED)
    val fileName = url.lastPathComponent ?: "studio-gallery.$extension"
    val path = NSTemporaryDirectory() + "classitda-studio-image-${NSUUID().UUIDString}.$extension"
    val copied =
        NSFileManager.defaultManager.copyItemAtPath(
            srcPath = sourcePath,
            toPath = path,
            error = null,
        )
    if (!copied) {
        return StudioImagePickerResult.Error(StudioImagePickerError.READ_FAILED)
    }
    val resourceValues =
        NSURL.fileURLWithPath(path).resourceValuesForKeys(
            listOf(NSURLFileSizeKey),
            error = null,
        )
    val sizeBytes =
        (resourceValues?.get(NSURLFileSizeKey) as? NSNumber)?.longLongValue
            ?: 0L
    if (sizeBytes <= 0L) {
        NSFileManager.defaultManager.removeItemAtPath(path, error = null)
        return StudioImagePickerResult.Error(StudioImagePickerError.READ_FAILED)
    }
    if (sizeBytes > STUDIO_IMAGE_MAX_SIZE_BYTES) {
        NSFileManager.defaultManager.removeItemAtPath(path, error = null)
        return StudioImagePickerResult.Error(StudioImagePickerError.FILE_TOO_LARGE)
    }
    return StudioImagePickerResult.Selected(
        StudioImagePickerSelection(
            handle = path,
            previewReference = path,
            mimeType = mimeType,
            fileName = fileName,
            sizeBytes = sizeBytes,
        ),
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun writeImageData(
    data: NSData,
    extension: String,
    mimeType: String,
    fileName: String,
): StudioImagePickerResult {
    val sizeBytes = data.length.toLong()
    val path = NSTemporaryDirectory() + "classitda-studio-image-${NSUUID().UUIDString}.$extension"
    if (!data.writeToFile(path, atomically = true)) {
        return StudioImagePickerResult.Error(StudioImagePickerError.READ_FAILED)
    }
    val selection =
        StudioImagePickerSelection(
            handle = path,
            previewReference = path,
            mimeType = mimeType,
            fileName = fileName,
            sizeBytes = sizeBytes,
        )
    val validationError = validateStudioImagePickerSelection(selection)
    if (validationError != null) {
        NSFileManager.defaultManager.removeItemAtPath(path, error = null)
        return StudioImagePickerResult.Error(validationError)
    }
    return StudioImagePickerResult.Selected(selection)
}

private fun String.toImageMimeType(): String? =
    when (this) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        else -> null
    }
