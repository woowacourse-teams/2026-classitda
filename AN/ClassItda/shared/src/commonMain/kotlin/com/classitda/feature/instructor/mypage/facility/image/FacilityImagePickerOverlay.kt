package com.classitda.feature.instructor.mypage.facility.image

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
import classitda.shared.generated.resources.instructor_facility_image_picker_camera
import classitda.shared.generated.resources.instructor_facility_image_picker_cancel
import classitda.shared.generated.resources.instructor_facility_image_picker_description
import classitda.shared.generated.resources.instructor_facility_image_picker_gallery
import classitda.shared.generated.resources.instructor_facility_image_picker_title
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.platform.FacilityImagePicker
import com.classitda.core.platform.FacilityImagePickerError
import com.classitda.core.platform.FacilityImagePickerResult
import com.classitda.core.platform.FacilityImagePickerSelection
import com.classitda.core.platform.FacilityImageSource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FacilityImagePickerOverlay(
    visible: Boolean,
    onSelected: (FacilityImagePickerSelection) -> Unit,
    onCancelled: () -> Unit,
    onError: (FacilityImagePickerError) -> Unit,
) {
    if (!visible) return

    var source by remember(visible) { mutableStateOf<FacilityImageSource?>(null) }
    val handleResult: (FacilityImagePickerResult) -> Unit = { result ->
        source = null
        when (result) {
            is FacilityImagePickerResult.Selected -> onSelected(result.selection)
            FacilityImagePickerResult.Cancelled -> onCancelled()
            is FacilityImagePickerResult.Error -> onError(result.reason)
        }
    }

    if (source == null) {
        AlertDialog(
            onDismissRequest = onCancelled,
            title = { Text(stringResource(Res.string.instructor_facility_image_picker_title)) },
            text = { Text(stringResource(Res.string.instructor_facility_image_picker_description)) },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Button(
                        onClick = { source = FacilityImageSource.CAMERA },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(Res.string.instructor_facility_image_picker_camera))
                    }
                    Button(
                        onClick = { source = FacilityImageSource.GALLERY },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(Res.string.instructor_facility_image_picker_gallery))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelled) {
                    Text(stringResource(Res.string.instructor_facility_image_picker_cancel))
                }
            },
        )
    } else {
        FacilityImagePicker(
            source = source!!,
            onResult = handleResult,
        )
    }
}
