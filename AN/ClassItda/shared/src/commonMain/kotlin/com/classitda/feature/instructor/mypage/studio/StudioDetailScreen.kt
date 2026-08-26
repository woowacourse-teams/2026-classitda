package com.classitda.feature.instructor.mypage.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_back
import classitda.shared.generated.resources.ic_close
import classitda.shared.generated.resources.ic_home
import classitda.shared.generated.resources.instructor_member_registration_success_confirm
import classitda.shared.generated.resources.instructor_studio_delete_cancel
import classitda.shared.generated.resources.instructor_studio_delete_confirm
import classitda.shared.generated.resources.instructor_studio_delete_failed
import classitda.shared.generated.resources.instructor_studio_delete_message
import classitda.shared.generated.resources.instructor_studio_delete_name_error
import classitda.shared.generated.resources.instructor_studio_delete_pane_title
import classitda.shared.generated.resources.instructor_studio_delete_placeholder
import classitda.shared.generated.resources.instructor_studio_delete_submitting
import classitda.shared.generated.resources.instructor_studio_delete_success
import classitda.shared.generated.resources.instructor_studio_delete_success_title
import classitda.shared.generated.resources.instructor_studio_delete_title
import classitda.shared.generated.resources.instructor_studio_detail_address
import classitda.shared.generated.resources.instructor_studio_detail_back
import classitda.shared.generated.resources.instructor_studio_detail_description
import classitda.shared.generated.resources.instructor_studio_detail_edit
import classitda.shared.generated.resources.instructor_studio_detail_error
import classitda.shared.generated.resources.instructor_studio_detail_loading
import classitda.shared.generated.resources.instructor_studio_detail_operating_hours
import classitda.shared.generated.resources.instructor_studio_detail_phone
import classitda.shared.generated.resources.instructor_studio_detail_retry
import classitda.shared.generated.resources.instructor_studio_detail_title
import classitda.shared.generated.resources.phone_number_change_close
import coil3.compose.SubcomposeAsyncImage
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.feature.instructor.mypage.contract.STUDIO_DELETE_ENABLED
import com.classitda.feature.instructor.mypage.contract.StudioDeleteError
import com.classitda.feature.instructor.mypage.contract.StudioDeleteState
import com.classitda.feature.instructor.mypage.contract.StudioDetailAction
import com.classitda.feature.instructor.mypage.contract.StudioDetailUiState
import com.classitda.feature.instructor.mypage.contract.StudioUiModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun StudioDetailScreen(
    uiState: StudioDetailUiState,
    onAction: (StudioDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var deleteSuccessVisible by remember { mutableStateOf(false) }
    var deleteSuccessPresented by remember { mutableStateOf(false) }
    LaunchedEffect(uiState) {
        if (uiState is StudioDetailUiState.Deleted) {
            deleteSuccessPresented = true
            deleteSuccessVisible = true
        }
    }
    LaunchedEffect(deleteSuccessVisible) {
        val deleted = uiState as? StudioDetailUiState.Deleted
        if (deleteSuccessPresented && !deleteSuccessVisible && deleted != null) {
            onAction(StudioDetailAction.DeleteAcknowledged(deleted.studioId))
        }
    }
    val content = uiState as? StudioDetailUiState.Content
    val isDeleting = STUDIO_DELETE_ENABLED && content?.deleteState is StudioDeleteState.Submitting

    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        topBar = {
            StudioDetailTopBar(
                onBack = { if (!isDeleting) onAction(StudioDetailAction.Back) },
            )
        },
        bottomBar = {
            if (content != null && !isDeleting) {
                StudioDetailActions(onAction = onAction)
            }
        },
    ) { innerPadding ->
        when (uiState) {
            StudioDetailUiState.Loading -> {
                StudioDetailStatus(
                    message = stringResource(Res.string.instructor_studio_detail_loading),
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is StudioDetailUiState.Error -> {
                StudioDetailError(
                    onRetry = { onAction(StudioDetailAction.Retry) },
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is StudioDetailUiState.Content -> {
                StudioDetailContent(
                    studio = uiState.studio,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is StudioDetailUiState.Deleted -> {
                StudioDetailStatus(
                    message = stringResource(Res.string.instructor_studio_delete_submitting),
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }

    if (STUDIO_DELETE_ENABLED && content != null) {
        StudioDeleteDialog(
            studioName = content.studio.name,
            state = content.deleteState,
            onAction = onAction,
        )
    }
    if (STUDIO_DELETE_ENABLED && uiState is StudioDetailUiState.Deleted && deleteSuccessVisible) {
        StudioDeleteSuccessDialog(onClose = { deleteSuccessVisible = false })
    }
}

@Composable
private fun StudioDetailTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_back),
                contentDescription = stringResource(Res.string.instructor_studio_detail_back),
                tint = InsColors.TextPrimary,
            )
        }
        Text(
            text = stringResource(Res.string.instructor_studio_detail_title),
            modifier = Modifier.weight(1f).semantics { heading() },
            style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
        )
        Spacer(modifier = Modifier.width(AppSpacing.xxl))
    }
}

@Composable
private fun StudioDetailContent(
    studio: StudioUiModel,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xxl),
    ) {
        item {
            StudioDetailImage(
                reference = studio.image?.previewReference,
                modifier = Modifier.fillMaxWidth().heightIn(min = AppSpacing.xxxl * 5),
            )
        }
        item {
            Text(
                text = studio.name,
                style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = InsColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShape.Card,
                color = InsColors.Surface,
            ) {
                Column(
                    modifier = Modifier.padding(AppSpacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xl),
                ) {
                    StudioDetailRow(
                        label = stringResource(Res.string.instructor_studio_detail_address),
                        value =
                            listOf(
                                studio.address.displayAddress,
                                studio.address.detailAddress,
                            ).filter(String::isNotBlank).joinToString(" "),
                    )
                    StudioDetailRow(
                        label = stringResource(Res.string.instructor_studio_detail_phone),
                        value = studio.phoneNumber.ifBlank { "-" },
                    )
                    StudioDetailRow(
                        label = stringResource(Res.string.instructor_studio_detail_operating_hours),
                        value =
                            listOf(studio.openingTime, studio.closingTime)
                                .filter(String::isNotBlank)
                                .joinToString(" - ")
                                .ifBlank { "-" },
                    )
                    StudioDetailRow(
                        label = stringResource(Res.string.instructor_studio_detail_description),
                        value = studio.description.ifBlank { "-" },
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioDetailRow(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(
            text = label,
            style = appTypography().labelLarge,
            color = InsColors.TextSecondary,
        )
        Text(
            text = value,
            style = appTypography().bodyLarge,
            color = InsColors.TextPrimary,
        )
    }
}

@Composable
private fun StudioDetailActions(onAction: (StudioDetailAction) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(AppSpacing.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Button(
            onClick = { onAction(StudioDetailAction.OpenEdit) },
            modifier = Modifier.fillMaxWidth().heightIn(min = AppSpacing.xxxl + AppSpacing.lg),
            colors = ButtonDefaults.buttonColors(containerColor = InsColors.Primary, contentColor = InsColors.White),
            shape = AppShape.Card,
        ) {
            Text(
                text = stringResource(Res.string.instructor_studio_detail_edit),
                style = appTypography().bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun StudioDeleteDialog(
    studioName: String,
    state: StudioDeleteState,
    onAction: (StudioDetailAction) -> Unit,
) {
    if (state == StudioDeleteState.Hidden) return
    val isSubmitting = state is StudioDeleteState.Submitting
    val typedName =
        when (state) {
            is StudioDeleteState.Confirming -> state.typedName
            is StudioDeleteState.Failed -> state.typedName
            else -> ""
        }
    val inputError =
        (typedName.isNotBlank() && typedName != studioName) ||
            when (state) {
                is StudioDeleteState.Confirming -> state.error == StudioDeleteError.NAME_MISMATCH
                is StudioDeleteState.Failed -> state.reason == StudioDeleteError.NAME_MISMATCH
                else -> false
            }
    val deleteDialogTitle = stringResource(Res.string.instructor_studio_delete_pane_title)
    val nameErrorMessage = stringResource(Res.string.instructor_studio_delete_name_error)
    androidx.compose.ui.window.Dialog(
        onDismissRequest = { if (!isSubmitting) onAction(StudioDetailAction.CancelDelete) },
        properties =
            DialogProperties(
                dismissOnBackPress = !isSubmitting,
                dismissOnClickOutside = !isSubmitting,
                usePlatformDefaultWidth = false,
            ),
    ) {
        Surface(
            modifier =
                Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xxxl).semantics {
                    paneTitle = deleteDialogTitle
                },
            shape = AppShape.Card,
            color = InsColors.Surface,
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                Text(
                    text = stringResource(Res.string.instructor_studio_delete_title),
                    style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = InsColors.TextPrimary,
                )
                Text(
                    text = stringResource(Res.string.instructor_studio_delete_message),
                    style = appTypography().bodyMedium,
                    color = InsColors.TextSecondary,
                )
                Text(
                    text = studioName,
                    style = appTypography().titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = InsColors.TextPrimary,
                )
                OutlinedTextField(
                    value = typedName,
                    onValueChange = { onAction(StudioDetailAction.DeleteNameChanged(it)) },
                    enabled = !isSubmitting,
                    modifier =
                        Modifier.fillMaxWidth().semantics {
                            if (inputError) error(nameErrorMessage)
                        },
                    placeholder = { Text(stringResource(Res.string.instructor_studio_delete_placeholder)) },
                    isError = inputError,
                    singleLine = true,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = InsColors.Primary,
                            unfocusedBorderColor = InsColors.Divider,
                            errorBorderColor = InsColors.Red,
                        ),
                )
                if (inputError) {
                    Text(
                        text = stringResource(Res.string.instructor_studio_delete_name_error),
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                        style = appTypography().bodySmall,
                        color = InsColors.Red,
                    )
                }
                if (state is StudioDeleteState.Failed && state.reason != StudioDeleteError.NAME_MISMATCH) {
                    Text(
                        text = stringResource(Res.string.instructor_studio_delete_failed),
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                        style = appTypography().bodySmall,
                        color = InsColors.Red,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    TextButton(
                        onClick = { onAction(StudioDetailAction.CancelDelete) },
                        enabled = !isSubmitting,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(Res.string.instructor_studio_delete_cancel))
                    }
                    Button(
                        onClick = { onAction(StudioDetailAction.ConfirmDelete) },
                        enabled = !isSubmitting && typedName == studioName,
                        modifier = Modifier.weight(1f),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = InsColors.Red,
                                contentColor = InsColors.White,
                            ),
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(AppSpacing.lg),
                                color = InsColors.White,
                                strokeWidth = AppSpacing.xs / 2,
                            )
                        } else {
                            Text(stringResource(Res.string.instructor_studio_delete_confirm))
                        }
                    }
                }
                if (isSubmitting) {
                    Text(
                        text = stringResource(Res.string.instructor_studio_delete_submitting),
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        style = appTypography().bodySmall,
                        color = InsColors.TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioDeleteSuccessDialog(onClose: () -> Unit) {
    val paneTitle = stringResource(Res.string.instructor_studio_delete_success_title)
    androidx.compose.ui.window.Dialog(
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
                    text = stringResource(Res.string.instructor_studio_delete_success),
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
private fun StudioDetailImage(
    reference: String?,
    modifier: Modifier = Modifier,
) {
    val imageModifier = modifier.clip(AppShape.Card)
    if (reference.isNullOrBlank()) {
        StudioDetailImageFallback(imageModifier)
    } else {
        SubcomposeAsyncImage(
            model = reference,
            contentDescription = null,
            modifier = imageModifier,
            loading = { StudioDetailImageFallback(Modifier.fillMaxSize()) },
            error = { StudioDetailImageFallback(Modifier.fillMaxSize()) },
        )
    }
}

@Composable
private fun StudioDetailImageFallback(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(InsColors.Gray200),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_home),
            contentDescription = null,
            tint = InsColors.TextTertiary,
            modifier = Modifier.size(AppSpacing.xxxl),
        )
    }
}

@Composable
private fun StudioDetailStatus(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = InsColors.Primary)
        Text(
            text = message,
            modifier = Modifier.padding(top = AppSpacing.lg),
            style = appTypography().bodyLarge,
            color = InsColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StudioDetailError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(AppSpacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.instructor_studio_detail_error),
            style = appTypography().bodyLarge,
            color = InsColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(Res.string.instructor_studio_detail_retry))
        }
    }
}

private val studioDetailFixture =
    StudioUiModel(
        id = InstructorStudioId("studio-preview"),
        name = "클래스잇다 스튜디오",
        address =
            com.classitda.domain.model.instructor.mypage.StudioAddress(
                roadAddress = "서울특별시 강남구 테헤란로",
                detailAddress = "5층 501호",
            ),
        phoneNumber = "02-1234-5678",
        description = "회원들이 편하게 운동할 수 있도록 운영하는 시설입니다.",
        openingTime = "09:00",
        closingTime = "22:00",
    )

@Preview(name = "Studio detail", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun StudioDetailScreenPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        StudioDetailScreen(
            uiState = StudioDetailUiState.Content(studioDetailFixture),
            onAction = {},
        )
    }
}

@Preview(name = "Delete confirmation", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun StudioDetailScreenPreview_DeleteConfirmation() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        StudioDetailScreen(
            uiState =
                StudioDetailUiState.Content(
                    studio = studioDetailFixture,
                    deleteState = StudioDeleteState.Confirming(),
                ),
            onAction = {},
        )
    }
}
