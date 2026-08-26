package com.classitda.feature.instructor.mypage.studio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_back
import classitda.shared.generated.resources.ic_camera
import classitda.shared.generated.resources.ic_close
import classitda.shared.generated.resources.ic_location_on
import classitda.shared.generated.resources.instructor_member_registration_success_confirm
import classitda.shared.generated.resources.instructor_studio_edit_back
import classitda.shared.generated.resources.instructor_studio_edit_error
import classitda.shared.generated.resources.instructor_studio_edit_submit
import classitda.shared.generated.resources.instructor_studio_edit_submitting
import classitda.shared.generated.resources.instructor_studio_edit_success
import classitda.shared.generated.resources.instructor_studio_edit_success_title
import classitda.shared.generated.resources.instructor_studio_edit_title
import classitda.shared.generated.resources.instructor_studio_registration_address
import classitda.shared.generated.resources.instructor_studio_registration_address_error
import classitda.shared.generated.resources.instructor_studio_registration_address_placeholder
import classitda.shared.generated.resources.instructor_studio_registration_address_search
import classitda.shared.generated.resources.instructor_studio_registration_back
import classitda.shared.generated.resources.instructor_studio_registration_closing_time
import classitda.shared.generated.resources.instructor_studio_registration_closing_time_error
import classitda.shared.generated.resources.instructor_studio_registration_closing_time_placeholder
import classitda.shared.generated.resources.instructor_studio_registration_description
import classitda.shared.generated.resources.instructor_studio_registration_description_placeholder
import classitda.shared.generated.resources.instructor_studio_registration_detail_address
import classitda.shared.generated.resources.instructor_studio_registration_detail_address_placeholder
import classitda.shared.generated.resources.instructor_studio_registration_error
import classitda.shared.generated.resources.instructor_studio_registration_image_camera_unavailable
import classitda.shared.generated.resources.instructor_studio_registration_image_file_too_large
import classitda.shared.generated.resources.instructor_studio_registration_image_invalid_mime
import classitda.shared.generated.resources.instructor_studio_registration_image_label
import classitda.shared.generated.resources.instructor_studio_registration_image_optional
import classitda.shared.generated.resources.instructor_studio_registration_image_permission_denied
import classitda.shared.generated.resources.instructor_studio_registration_image_read_failed
import classitda.shared.generated.resources.instructor_studio_registration_image_remove
import classitda.shared.generated.resources.instructor_studio_registration_image_upload_unavailable
import classitda.shared.generated.resources.instructor_studio_registration_intro_description
import classitda.shared.generated.resources.instructor_studio_registration_intro_title
import classitda.shared.generated.resources.instructor_studio_registration_loading
import classitda.shared.generated.resources.instructor_studio_registration_name
import classitda.shared.generated.resources.instructor_studio_registration_name_error
import classitda.shared.generated.resources.instructor_studio_registration_name_placeholder
import classitda.shared.generated.resources.instructor_studio_registration_opening_time
import classitda.shared.generated.resources.instructor_studio_registration_opening_time_error
import classitda.shared.generated.resources.instructor_studio_registration_opening_time_placeholder
import classitda.shared.generated.resources.instructor_studio_registration_phone
import classitda.shared.generated.resources.instructor_studio_registration_phone_error
import classitda.shared.generated.resources.instructor_studio_registration_phone_placeholder
import classitda.shared.generated.resources.instructor_studio_registration_register
import classitda.shared.generated.resources.instructor_studio_registration_retry
import classitda.shared.generated.resources.instructor_studio_registration_step
import classitda.shared.generated.resources.instructor_studio_registration_submitting
import classitda.shared.generated.resources.instructor_studio_registration_success
import classitda.shared.generated.resources.instructor_studio_registration_title
import classitda.shared.generated.resources.phone_number_change_close
import coil3.compose.SubcomposeAsyncImage
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.instructor.mypage.StudioAddress
import com.classitda.domain.model.instructor.mypage.StudioImageSelection
import com.classitda.feature.instructor.mypage.contract.StudioImageInputUiModel
import com.classitda.feature.instructor.mypage.contract.StudioImageUiError
import com.classitda.feature.instructor.mypage.contract.StudioInputUiModel
import com.classitda.feature.instructor.mypage.contract.StudioRegistrationAction
import com.classitda.feature.instructor.mypage.contract.StudioRegistrationField
import com.classitda.feature.instructor.mypage.contract.StudioRegistrationUiState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun StudioRegistrationScreen(
    uiState: StudioRegistrationUiState,
    onAction: (StudioRegistrationAction) -> Unit,
    modifier: Modifier = Modifier,
    isEditing: Boolean = false,
    onSuccessAcknowledged: () -> Unit = {},
) {
    var successDialogVisible by remember { mutableStateOf(false) }
    var successDialogPresented by remember { mutableStateOf(false) }
    LaunchedEffect(uiState, isEditing) {
        if (isEditing && uiState is StudioRegistrationUiState.Success) {
            successDialogPresented = true
            successDialogVisible = true
        }
    }
    LaunchedEffect(successDialogVisible) {
        val success = uiState as? StudioRegistrationUiState.Success
        if (isEditing && successDialogPresented && !successDialogVisible && success != null) {
            onSuccessAcknowledged()
        }
    }
    val isSubmitting = uiState is StudioRegistrationUiState.Submitting
    val isLoading = uiState is StudioRegistrationUiState.Loading
    val draft =
        when (uiState) {
            is StudioRegistrationUiState.Editing -> uiState.draft
            is StudioRegistrationUiState.Error -> uiState.draft
            else -> StudioInputUiModel()
        }
    val fieldErrors =
        (uiState as? StudioRegistrationUiState.Editing)?.fieldErrors.orEmpty()
    val imageError =
        (uiState as? StudioRegistrationUiState.Editing)?.imageError
    val canAttemptSubmit =
        uiState is StudioRegistrationUiState.Editing || uiState is StudioRegistrationUiState.Error

    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        topBar = {
            StudioRegistrationTopBar(
                onBack = { if (!isSubmitting) onAction(StudioRegistrationAction.Back) },
                title =
                    stringResource(
                        if (isEditing) {
                            Res.string.instructor_studio_edit_title
                        } else {
                            Res.string.instructor_studio_registration_title
                        },
                    ),
                backDescription =
                    stringResource(
                        if (isEditing) {
                            Res.string.instructor_studio_edit_back
                        } else {
                            Res.string.instructor_studio_registration_back
                        },
                    ),
            )
        },
        bottomBar = {
            if (!isLoading && !isSubmitting) {
                StudioRegistrationBottomBar(
                    isSubmitting = false,
                    isFailed = uiState is StudioRegistrationUiState.Error,
                    enabled = canAttemptSubmit,
                    label =
                        stringResource(
                            if (isEditing) {
                                Res.string.instructor_studio_edit_submit
                            } else {
                                Res.string.instructor_studio_registration_register
                            },
                        ),
                    submittingLabel =
                        stringResource(
                            if (isEditing) {
                                Res.string.instructor_studio_edit_submitting
                            } else {
                                Res.string.instructor_studio_registration_submitting
                            },
                        ),
                    onSubmit = {
                        onAction(
                            if (uiState is StudioRegistrationUiState.Error) {
                                StudioRegistrationAction.Retry
                            } else {
                                StudioRegistrationAction.Submit
                            },
                        )
                    },
                )
            }
        },
    ) { innerPadding ->
        when (uiState) {
            StudioRegistrationUiState.Loading -> {
                StudioRegistrationStatus(
                    message = stringResource(Res.string.instructor_studio_registration_loading),
                    modifier = Modifier.padding(innerPadding),
                )
            }

            StudioRegistrationUiState.Submitting -> {
                StudioRegistrationStatus(
                    message =
                        stringResource(
                            if (isEditing) {
                                Res.string.instructor_studio_edit_submitting
                            } else {
                                Res.string.instructor_studio_registration_submitting
                            },
                        ),
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is StudioRegistrationUiState.Success -> {
                StudioRegistrationStatus(
                    message =
                        stringResource(
                            if (isEditing) {
                                Res.string.instructor_studio_edit_success
                            } else {
                                Res.string.instructor_studio_registration_success
                            },
                        ),
                    modifier = Modifier.padding(innerPadding),
                )
            }

            else -> {
                StudioRegistrationForm(
                    draft = draft,
                    fieldErrors = fieldErrors,
                    imageError = imageError,
                    isSubmitting = false,
                    errorMessage =
                        if (uiState is StudioRegistrationUiState.Error) {
                            stringResource(
                                if (isEditing) {
                                    Res.string.instructor_studio_edit_error
                                } else {
                                    Res.string.instructor_studio_registration_error
                                },
                            )
                        } else {
                            null
                        },
                    onAction = onAction,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
    if (isEditing && uiState is StudioRegistrationUiState.Success && successDialogVisible) {
        StudioEditSuccessDialog(onClose = { successDialogVisible = false })
    }
}

@Composable
private fun StudioEditSuccessDialog(onClose: () -> Unit) {
    val paneTitle = stringResource(Res.string.instructor_studio_edit_success_title)
    Dialog(
        onDismissRequest = {},
        properties =
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
            ),
    ) {
        Surface(
            modifier =
                Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xxxl).semantics {
                    this.paneTitle = paneTitle
                },
            shape = AppShape.Card,
            color = InsColors.Surface,
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = paneTitle,
                        modifier = Modifier.align(Alignment.Center),
                        style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = InsColors.TextPrimary,
                    )
                    IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopEnd)) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_close),
                            contentDescription = stringResource(Res.string.phone_number_change_close),
                            tint = InsColors.TextSecondary,
                        )
                    }
                }
                Text(
                    text = stringResource(Res.string.instructor_studio_edit_success),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                    style = appTypography().bodyLarge,
                    color = InsColors.TextSecondary,
                )
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShape.Card,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = InsColors.Primary,
                            contentColor = InsColors.White,
                        ),
                ) {
                    Text(stringResource(Res.string.instructor_member_registration_success_confirm))
                }
            }
        }
    }
}

@Composable
private fun StudioRegistrationTopBar(
    onBack: () -> Unit,
    title: String,
    backDescription: String,
) {
    Box(
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_back),
                contentDescription = backDescription,
                tint = InsColors.TextPrimary,
            )
        }
        Text(
            text = title,
            modifier = Modifier.semantics { heading() },
            style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
        )
    }
}

@Composable
private fun StudioRegistrationForm(
    draft: StudioInputUiModel,
    fieldErrors: Set<StudioRegistrationField>,
    imageError: StudioImageUiError?,
    isSubmitting: Boolean,
    errorMessage: String?,
    onAction: (StudioRegistrationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val nameError = StudioRegistrationField.NAME in fieldErrors
    val addressError = StudioRegistrationField.ADDRESS in fieldErrors
    val detailAddressError = StudioRegistrationField.DETAIL_ADDRESS in fieldErrors
    val phoneError = StudioRegistrationField.PHONE_NUMBER in fieldErrors
    val openingTimeError = StudioRegistrationField.OPENING_TIME in fieldErrors
    val closingTimeError = StudioRegistrationField.CLOSING_TIME in fieldErrors
    val descriptionError = StudioRegistrationField.DESCRIPTION in fieldErrors

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.xxl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xxl),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                text = stringResource(Res.string.instructor_studio_registration_intro_title),
                style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
                color = InsColors.TextPrimary,
            )
            Text(
                text = stringResource(Res.string.instructor_studio_registration_intro_description),
                style = appTypography().bodyLarge,
                color = InsColors.TextSecondary,
            )
        }
        StudioImageSection(
            image = draft.image,
            enabled = !isSubmitting,
            onRequestImageSource = { onAction(StudioRegistrationAction.RequestImageSource) },
            onRemoveImage = { onAction(StudioRegistrationAction.RemoveImage) },
        )
        val displayedImageError =
            if (imageError != null) {
                imageError
            } else if (StudioRegistrationField.IMAGE in fieldErrors) {
                StudioImageUiError.UPLOAD_UNAVAILABLE
            } else {
                null
            }
        val displayedImageErrorText =
            displayedImageError?.let { error ->
                stringResource(
                    when (error) {
                        StudioImageUiError.PERMISSION_DENIED -> {
                            Res.string.instructor_studio_registration_image_permission_denied
                        }

                        StudioImageUiError.CAMERA_UNAVAILABLE -> {
                            Res.string.instructor_studio_registration_image_camera_unavailable
                        }

                        StudioImageUiError.READ_FAILED -> {
                            Res.string.instructor_studio_registration_image_read_failed
                        }

                        StudioImageUiError.INVALID_MIME -> {
                            Res.string.instructor_studio_registration_image_invalid_mime
                        }

                        StudioImageUiError.FILE_TOO_LARGE -> {
                            Res.string.instructor_studio_registration_image_file_too_large
                        }

                        StudioImageUiError.UPLOAD_UNAVAILABLE -> {
                            Res.string.instructor_studio_registration_image_upload_unavailable
                        }
                    },
                )
            }
        displayedImageErrorText?.let { errorMessageText ->
            Text(
                text = errorMessageText,
                modifier = Modifier.semantics { error(errorMessageText) },
                style = appTypography().bodySmall,
                color = InsColors.Red,
            )
        }
        StudioTextField(
            label = stringResource(Res.string.instructor_studio_registration_name),
            value = draft.name,
            placeholder = stringResource(Res.string.instructor_studio_registration_name_placeholder),
            isError = nameError,
            errorMessage =
                if (nameError) {
                    stringResource(
                        Res.string.instructor_studio_registration_name_error,
                    )
                } else {
                    null
                },
            enabled = !isSubmitting,
            onValueChange = { onAction(StudioRegistrationAction.NameChanged(it)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            StudioTextField(
                label = stringResource(Res.string.instructor_studio_registration_address),
                value = draft.address.displayAddress,
                placeholder = stringResource(Res.string.instructor_studio_registration_address_placeholder),
                isError = addressError,
                errorMessage =
                    if (addressError) {
                        stringResource(
                            Res.string.instructor_studio_registration_address_error,
                        )
                    } else {
                        null
                    },
                enabled = !isSubmitting,
                onValueChange = { onAction(StudioRegistrationAction.AddressChanged(it)) },
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = { onAction(StudioRegistrationAction.RequestAddressSearch) },
                enabled = !isSubmitting,
                modifier = Modifier.padding(top = AppSpacing.xxl + AppSpacing.sm),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_location_on),
                    contentDescription = null,
                    tint = InsColors.Purple,
                    modifier = Modifier.size(AppSpacing.lg),
                )
                Text(
                    text = stringResource(Res.string.instructor_studio_registration_address_search),
                    modifier = Modifier.padding(start = AppSpacing.xs),
                    color = InsColors.Purple,
                )
            }
        }
        StudioTextField(
            label = stringResource(Res.string.instructor_studio_registration_detail_address),
            value = draft.address.detailAddress,
            placeholder = stringResource(Res.string.instructor_studio_registration_detail_address_placeholder),
            isError = detailAddressError,
            errorMessage = null,
            enabled = !isSubmitting,
            onValueChange = { onAction(StudioRegistrationAction.DetailAddressChanged(it)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        )
        StudioTextField(
            label = stringResource(Res.string.instructor_studio_registration_phone),
            value = draft.phoneNumber,
            placeholder = stringResource(Res.string.instructor_studio_registration_phone_placeholder),
            isError = phoneError,
            errorMessage =
                if (phoneError) {
                    stringResource(
                        Res.string.instructor_studio_registration_phone_error,
                    )
                } else {
                    null
                },
            enabled = !isSubmitting,
            onValueChange = { onAction(StudioRegistrationAction.PhoneNumberChanged(it)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            StudioTextField(
                label = stringResource(Res.string.instructor_studio_registration_opening_time),
                value = draft.openingTime,
                placeholder = stringResource(Res.string.instructor_studio_registration_opening_time_placeholder),
                isError = openingTimeError,
                errorMessage =
                    if (openingTimeError) {
                        stringResource(Res.string.instructor_studio_registration_opening_time_error)
                    } else {
                        null
                    },
                enabled = !isSubmitting,
                onValueChange = { onAction(StudioRegistrationAction.OpeningTimeChanged(it)) },
                modifier = Modifier.weight(1f),
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Next,
                    ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
            )
            StudioTextField(
                label = stringResource(Res.string.instructor_studio_registration_closing_time),
                value = draft.closingTime,
                placeholder = stringResource(Res.string.instructor_studio_registration_closing_time_placeholder),
                isError = closingTimeError,
                errorMessage =
                    if (closingTimeError) {
                        stringResource(Res.string.instructor_studio_registration_closing_time_error)
                    } else {
                        null
                    },
                enabled = !isSubmitting,
                onValueChange = { onAction(StudioRegistrationAction.ClosingTimeChanged(it)) },
                modifier = Modifier.weight(1f),
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Next,
                    ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            )
        }
        StudioTextField(
            label = stringResource(Res.string.instructor_studio_registration_description),
            value = draft.description,
            placeholder = stringResource(Res.string.instructor_studio_registration_description_placeholder),
            isError = descriptionError,
            errorMessage = null,
            enabled = !isSubmitting,
            onValueChange = { onAction(StudioRegistrationAction.DescriptionChanged(it)) },
            singleLine = false,
            minLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
        )
        errorMessage?.let { message ->
            Text(
                text = message,
                modifier =
                    Modifier.semantics {
                        error(message)
                        liveRegion = LiveRegionMode.Assertive
                    },
                style = appTypography().bodySmall,
                color = InsColors.Red,
            )
            TextButton(onClick = { onAction(StudioRegistrationAction.Retry) }) {
                Text(stringResource(Res.string.instructor_studio_registration_retry))
            }
        }
    }
}

@Composable
private fun StudioImageSection(
    image: StudioImageInputUiModel?,
    enabled: Boolean,
    onRequestImageSource: () -> Unit,
    onRemoveImage: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.instructor_studio_registration_image_label),
                style = appTypography().titleMedium.copy(fontWeight = FontWeight.Bold),
                color = InsColors.TextPrimary,
            )
            Text(
                text = stringResource(Res.string.instructor_studio_registration_image_optional),
                style = appTypography().titleMedium.copy(fontWeight = FontWeight.Bold),
                color = InsColors.TextTertiary,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            StudioImageTile(
                image = image,
                enabled = enabled,
                onClick = onRequestImageSource,
                onRemove = if (image == null) null else onRemoveImage,
            )
        }
    }
}

@Composable
private fun StudioImageTile(
    image: StudioImageInputUiModel?,
    enabled: Boolean,
    onClick: () -> Unit,
    onRemove: (() -> Unit)? = null,
) {
    val tileModifier =
        Modifier
            .size(AppSpacing.xxxl * 3)
            .clip(AppShape.Card)
            .then(
                if (image == null) {
                    Modifier.border(BorderStroke(AppSpacing.xs / 2, InsColors.DividerStrong), AppShape.Card)
                } else {
                    Modifier
                },
            ).clickableIf(enabled, onClick)
    if (image == null) {
        Column(
            modifier = tileModifier.background(InsColors.SurfaceVariant),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_camera),
                contentDescription = stringResource(Res.string.instructor_studio_registration_image_label),
                tint = InsColors.TextTertiary,
                modifier = Modifier.size(AppSpacing.xxxl),
            )
        }
    } else {
        Box(modifier = tileModifier) {
            SubcomposeAsyncImage(
                model = image.previewReference,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                loading = { RegistrationStudioImageFallback(Modifier.fillMaxSize()) },
                error = { RegistrationStudioImageFallback(Modifier.fillMaxSize()) },
            )
            if (enabled && onRemove != null) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_close),
                        contentDescription = stringResource(Res.string.instructor_studio_registration_image_remove),
                        tint = InsColors.TextPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioTextField(
    label: String,
    value: String,
    placeholder: String,
    isError: Boolean,
    errorMessage: String?,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text(
            text = label,
            style = appTypography().titleMedium.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier =
                Modifier.fillMaxWidth().semantics {
                    if (isError && errorMessage != null) error(errorMessage)
                },
            enabled = enabled,
            placeholder = { Text(placeholder, color = InsColors.TextTertiary) },
            textStyle = appTypography().bodyLarge.copy(color = InsColors.TextPrimary),
            singleLine = singleLine,
            minLines = minLines,
            isError = isError,
            shape = AppShape.Card,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = studioFieldColors(),
        )
        errorMessage?.let { StudioFieldError(it) }
    }
}

@Composable
private fun RegistrationStudioImageFallback(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(InsColors.Gray200),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_camera),
            contentDescription = null,
            tint = InsColors.TextTertiary,
            modifier = Modifier.size(AppSpacing.xxxl),
        )
    }
}

@Composable
private fun StudioFieldError(message: String) {
    Text(
        text = message,
        style = appTypography().bodySmall,
        color = InsColors.Red,
    )
}

@Composable
private fun studioFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = InsColors.SurfaceVariant,
        unfocusedContainerColor = InsColors.SurfaceVariant,
        disabledContainerColor = InsColors.SurfaceVariant,
        focusedBorderColor = InsColors.TextPrimary,
        unfocusedBorderColor = InsColors.Divider,
        disabledBorderColor = InsColors.Divider,
        errorBorderColor = InsColors.Red,
        cursorColor = InsColors.TextPrimary,
        errorCursorColor = InsColors.Red,
    )

@Composable
private fun StudioRegistrationBottomBar(
    isSubmitting: Boolean,
    isFailed: Boolean,
    enabled: Boolean,
    label: String,
    submittingLabel: String,
    onSubmit: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding(),
        color = InsColors.Background,
    ) {
        Button(
            onClick = onSubmit,
            enabled = if (isFailed) !isSubmitting else enabled && !isSubmitting,
            modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.lg),
            shape = AppShape.Card,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = InsColors.Primary,
                    contentColor = InsColors.White,
                    disabledContainerColor = InsColors.SurfaceVariant,
                    disabledContentColor = InsColors.TextTertiary,
                ),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(AppSpacing.lg),
                    color = InsColors.TextTertiary,
                    strokeWidth = AppSpacing.xs / 2,
                )
                Text(
                    text = submittingLabel,
                    modifier = Modifier.padding(start = AppSpacing.xs),
                )
            } else {
                Text(
                    text = label,
                    style = appTypography().bodyLarge.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

@Composable
private fun StudioRegistrationStatus(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = InsColors.Purple)
        Text(
            text = message,
            modifier = Modifier.padding(top = AppSpacing.lg),
            style = appTypography().bodyMedium,
            color = InsColors.TextSecondary,
        )
    }
}

private fun Modifier.clickableIf(
    enabled: Boolean,
    onClick: () -> Unit,
): Modifier =
    if (enabled) {
        clickable(onClick = onClick)
    } else {
        this
    }

private val emptyStudioRegistrationState =
    StudioRegistrationUiState.Editing(
        draft = StudioInputUiModel(),
        canSubmit = false,
    )

private val filledStudioRegistrationState =
    StudioRegistrationUiState.Editing(
        draft =
            StudioInputUiModel(
                name = "더 에이치 휘트니스 강남점",
                address =
                    StudioAddress(
                        roadAddress = "서울 강남구 테헤란로 123",
                        detailAddress = "2층",
                    ),
                phoneNumber = "0212345678",
                description = "회원들이 편하게 운동할 수 있는 시설입니다.",
            ),
        canSubmit = true,
    )

private val singleImageStudioRegistrationState =
    StudioRegistrationUiState.Editing(
        draft =
            filledStudioRegistrationState.draft.copy(
                image =
                    StudioImageInputUiModel(
                        StudioImageSelection.Local(
                            handle = "fixture-image-handle",
                            previewReference = "fixture-image-reference",
                            mimeType = "image/jpeg",
                            fileName = "studio.jpg",
                            sizeBytes = 1024,
                        ),
                    ),
            ),
        canSubmit = false,
        fieldErrors = setOf(StudioRegistrationField.IMAGE),
        imageError = StudioImageUiError.UPLOAD_UNAVAILABLE,
    )

private val remoteImageStudioRegistrationState =
    StudioRegistrationUiState.Editing(
        draft =
            filledStudioRegistrationState.draft.copy(
                image =
                    StudioImageInputUiModel(
                        StudioImageSelection.Remote("remote-fixture-image"),
                    ),
            ),
        canSubmit = true,
    )

@Preview(
    name = "Empty form · Instructor",
    group = "Screen/StudioRegistration",
    widthDp = 390,
    heightDp = 1043,
)
@Composable
private fun StudioRegistrationScreenPreview_Empty() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        StudioRegistrationScreen(emptyStudioRegistrationState, onAction = {})
    }
}

@Preview(
    name = "Filled form · Instructor",
    group = "Screen/StudioRegistration",
    widthDp = 390,
    heightDp = 1043,
)
@Composable
private fun StudioRegistrationScreenPreview_Filled() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        StudioRegistrationScreen(filledStudioRegistrationState, onAction = {})
    }
}

@Preview(
    name = "Single image · Instructor",
    group = "Screen/StudioRegistration",
    widthDp = 390,
    heightDp = 1043,
)
@Composable
private fun StudioRegistrationScreenPreview_SingleImage() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        StudioRegistrationScreen(singleImageStudioRegistrationState, onAction = {})
    }
}

@Preview(
    name = "Remote image · Instructor",
    group = "Screen/StudioRegistration",
    widthDp = 390,
    heightDp = 1043,
)
@Composable
private fun StudioRegistrationScreenPreview_RemoteImage() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        StudioRegistrationScreen(remoteImageStudioRegistrationState, onAction = {})
    }
}

@Preview(
    name = "Field errors · Instructor",
    group = "Screen/StudioRegistration",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun StudioRegistrationScreenPreview_Errors() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        StudioRegistrationScreen(
            StudioRegistrationUiState.Editing(
                draft = StudioInputUiModel(name = "", phoneNumber = "010"),
                canSubmit = false,
                fieldErrors =
                    setOf(
                        StudioRegistrationField.NAME,
                        StudioRegistrationField.ADDRESS,
                        StudioRegistrationField.PHONE_NUMBER,
                    ),
            ),
            onAction = {},
        )
    }
}

@Preview(
    name = "Submitting · Instructor",
    group = "Screen/StudioRegistration",
)
@Composable
private fun StudioRegistrationScreenPreview_Submitting() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        StudioRegistrationScreen(StudioRegistrationUiState.Submitting, onAction = {})
    }
}

@Preview(
    name = "Submit failed · Instructor",
    group = "Screen/StudioRegistration",
)
@Composable
private fun StudioRegistrationScreenPreview_Failed() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        StudioRegistrationScreen(
            StudioRegistrationUiState.Error(
                draft = filledStudioRegistrationState.draft,
                reason = com.classitda.feature.instructor.mypage.contract.StudioRegistrationUiError.NETWORK,
            ),
            onAction = {},
        )
    }
}

@Preview(
    name = "Large text and keyboard boundary · Instructor",
    group = "Screen/StudioRegistration",
    widthDp = 320,
    heightDp = 560,
    fontScale = 1.5f,
)
@Composable
private fun StudioRegistrationScreenPreview_LargeText() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        StudioRegistrationScreen(filledStudioRegistrationState, onAction = {})
    }
}
