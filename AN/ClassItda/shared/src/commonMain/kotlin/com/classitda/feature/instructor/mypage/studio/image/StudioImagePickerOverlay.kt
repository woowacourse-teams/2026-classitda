package com.classitda.feature.instructor.mypage.studio.image

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.instructor_studio_image_picker_camera
import classitda.shared.generated.resources.instructor_studio_image_picker_cancel
import classitda.shared.generated.resources.instructor_studio_image_picker_description
import classitda.shared.generated.resources.instructor_studio_image_picker_gallery
import classitda.shared.generated.resources.instructor_studio_image_picker_title
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.platform.StudioImagePicker
import com.classitda.core.platform.StudioImagePickerError
import com.classitda.core.platform.StudioImagePickerResult
import com.classitda.core.platform.StudioImagePickerSelection
import com.classitda.core.platform.StudioImageSource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StudioImagePickerOverlay(
    visible: Boolean,
    onSelected: (StudioImagePickerSelection) -> Unit,
    onCancelled: () -> Unit,
    onError: (StudioImagePickerError) -> Unit,
) {
    if (!visible) return

    var source by remember(visible) { mutableStateOf<StudioImageSource?>(null) }
    val handleResult: (StudioImagePickerResult) -> Unit = { result ->
        source = null
        when (result) {
            is StudioImagePickerResult.Selected -> onSelected(result.selection)
            StudioImagePickerResult.Cancelled -> onCancelled()
            is StudioImagePickerResult.Error -> onError(result.reason)
        }
    }

    if (source == null) {
        AlertDialog(
            onDismissRequest = onCancelled,
            title = { Text(stringResource(Res.string.instructor_studio_image_picker_title)) },
            text = { Text(stringResource(Res.string.instructor_studio_image_picker_description)) },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Button(
                        onClick = { source = StudioImageSource.CAMERA },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(Res.string.instructor_studio_image_picker_camera))
                    }
                    Button(
                        onClick = { source = StudioImageSource.GALLERY },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(Res.string.instructor_studio_image_picker_gallery))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelled) {
                    Text(stringResource(Res.string.instructor_studio_image_picker_cancel))
                }
            },
        )
    } else {
        StudioImagePicker(
            source = source!!,
            onResult = handleResult,
        )
    }
}
