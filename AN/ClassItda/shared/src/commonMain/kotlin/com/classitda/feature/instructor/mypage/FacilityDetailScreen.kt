package com.classitda.feature.instructor.mypage

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
import classitda.shared.generated.resources.ic_home
import classitda.shared.generated.resources.instructor_facility_delete_cancel
import classitda.shared.generated.resources.instructor_facility_delete_confirm
import classitda.shared.generated.resources.instructor_facility_delete_failed
import classitda.shared.generated.resources.instructor_facility_delete_message
import classitda.shared.generated.resources.instructor_facility_delete_name_error
import classitda.shared.generated.resources.instructor_facility_delete_pane_title
import classitda.shared.generated.resources.instructor_facility_delete_placeholder
import classitda.shared.generated.resources.instructor_facility_delete_submitting
import classitda.shared.generated.resources.instructor_facility_delete_title
import classitda.shared.generated.resources.instructor_facility_detail_address
import classitda.shared.generated.resources.instructor_facility_detail_back
import classitda.shared.generated.resources.instructor_facility_detail_delete
import classitda.shared.generated.resources.instructor_facility_detail_description
import classitda.shared.generated.resources.instructor_facility_detail_edit
import classitda.shared.generated.resources.instructor_facility_detail_error
import classitda.shared.generated.resources.instructor_facility_detail_information
import classitda.shared.generated.resources.instructor_facility_detail_loading
import classitda.shared.generated.resources.instructor_facility_detail_operating_hours
import classitda.shared.generated.resources.instructor_facility_detail_phone
import classitda.shared.generated.resources.instructor_facility_detail_retry
import classitda.shared.generated.resources.instructor_facility_detail_title
import coil3.compose.SubcomposeAsyncImage
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.ManagedFacility
import com.classitda.feature.instructor.mypage.contract.FacilityDeleteError
import com.classitda.feature.instructor.mypage.contract.FacilityDeleteState
import com.classitda.feature.instructor.mypage.contract.FacilityDetailAction
import com.classitda.feature.instructor.mypage.contract.FacilityDetailUiState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun FacilityDetailScreen(
    uiState: FacilityDetailUiState,
    onAction: (FacilityDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = uiState as? FacilityDetailUiState.Content
    val isDeleting = content?.deleteState is FacilityDeleteState.Submitting

    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        topBar = {
            FacilityDetailTopBar(
                onBack = { if (!isDeleting) onAction(FacilityDetailAction.Back) },
            )
        },
        bottomBar = {
            if (content != null && !isDeleting) {
                FacilityDetailActions(onAction = onAction)
            }
        },
    ) { innerPadding ->
        when (uiState) {
            FacilityDetailUiState.Loading -> {
                FacilityDetailStatus(
                    message = stringResource(Res.string.instructor_facility_detail_loading),
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is FacilityDetailUiState.Error -> {
                FacilityDetailError(
                    onRetry = { onAction(FacilityDetailAction.Retry) },
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is FacilityDetailUiState.Content -> {
                FacilityDetailContent(
                    facility = uiState.facility,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is FacilityDetailUiState.Deleted -> {
                FacilityDetailStatus(
                    message = stringResource(Res.string.instructor_facility_delete_submitting),
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }

    if (content != null) {
        FacilityDeleteDialog(
            facilityName = content.facility.name,
            state = content.deleteState,
            onAction = onAction,
        )
    }
}

@Composable
private fun FacilityDetailTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_back),
                contentDescription = stringResource(Res.string.instructor_facility_detail_back),
                tint = InsColors.TextPrimary,
            )
        }
        Text(
            text = stringResource(Res.string.instructor_facility_detail_title),
            modifier = Modifier.weight(1f).semantics { heading() },
            style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
        )
        Spacer(modifier = Modifier.width(AppSpacing.xxl))
    }
}

@Composable
private fun FacilityDetailContent(
    facility: ManagedFacility,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xxl),
    ) {
        item {
            FacilityDetailImage(
                reference = facility.images.firstOrNull()?.previewReference ?: facility.representativeImageReference,
                modifier = Modifier.fillMaxWidth().heightIn(min = AppSpacing.xxxl * 5),
            )
        }
        item {
            Text(
                text = facility.name,
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
                    Text(
                        text = stringResource(Res.string.instructor_facility_detail_information),
                        style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = InsColors.TextPrimary,
                    )
                    FacilityDetailRow(
                        label = stringResource(Res.string.instructor_facility_detail_address),
                        value =
                            listOf(
                                facility.address,
                                facility.detailAddress,
                            ).filter(String::isNotBlank).joinToString(" "),
                    )
                    FacilityDetailRow(
                        label = stringResource(Res.string.instructor_facility_detail_phone),
                        value = formatFacilityPhone(facility.phoneNumber),
                    )
                    FacilityDetailRow(
                        label = stringResource(Res.string.instructor_facility_detail_operating_hours),
                        value =
                            listOf(facility.openingTime, facility.closingTime)
                                .filter(String::isNotBlank)
                                .joinToString(" - ")
                                .ifBlank { "-" },
                    )
                    FacilityDetailRow(
                        label = stringResource(Res.string.instructor_facility_detail_description),
                        value = facility.description.ifBlank { "-" },
                    )
                }
            }
        }
    }
}

@Composable
private fun FacilityDetailRow(
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
private fun FacilityDetailActions(onAction: (FacilityDetailAction) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(AppSpacing.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        TextButton(
            onClick = { onAction(FacilityDetailAction.RequestDelete) },
            modifier = Modifier.weight(1f).heightIn(min = AppSpacing.xxxl + AppSpacing.lg),
        ) {
            Text(
                text = stringResource(Res.string.instructor_facility_detail_delete),
                color = InsColors.Red,
                style = appTypography().bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
        Button(
            onClick = { onAction(FacilityDetailAction.OpenEdit) },
            modifier = Modifier.weight(1f).heightIn(min = AppSpacing.xxxl + AppSpacing.lg),
            colors = ButtonDefaults.buttonColors(containerColor = InsColors.Primary, contentColor = InsColors.White),
            shape = AppShape.Card,
        ) {
            Text(
                text = stringResource(Res.string.instructor_facility_detail_edit),
                style = appTypography().bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun FacilityDeleteDialog(
    facilityName: String,
    state: FacilityDeleteState,
    onAction: (FacilityDetailAction) -> Unit,
) {
    if (state == FacilityDeleteState.Hidden) return
    val isSubmitting = state is FacilityDeleteState.Submitting
    val typedName =
        when (state) {
            is FacilityDeleteState.Confirming -> state.typedName
            is FacilityDeleteState.Failed -> state.typedName
            else -> ""
        }
    val inputError =
        (typedName.isNotBlank() && typedName != facilityName) ||
            when (state) {
                is FacilityDeleteState.Confirming -> state.error == FacilityDeleteError.NAME_MISMATCH
                is FacilityDeleteState.Failed -> state.reason == FacilityDeleteError.NAME_MISMATCH
                else -> false
            }
    val deleteDialogTitle = stringResource(Res.string.instructor_facility_delete_pane_title)
    val nameErrorMessage = stringResource(Res.string.instructor_facility_delete_name_error)
    androidx.compose.ui.window.Dialog(
        onDismissRequest = { if (!isSubmitting) onAction(FacilityDetailAction.CancelDelete) },
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
                    text = stringResource(Res.string.instructor_facility_delete_title),
                    style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = InsColors.TextPrimary,
                )
                Text(
                    text = stringResource(Res.string.instructor_facility_delete_message),
                    style = appTypography().bodyMedium,
                    color = InsColors.TextSecondary,
                )
                Text(
                    text = facilityName,
                    style = appTypography().titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = InsColors.TextPrimary,
                )
                OutlinedTextField(
                    value = typedName,
                    onValueChange = { onAction(FacilityDetailAction.DeleteNameChanged(it)) },
                    enabled = !isSubmitting,
                    modifier =
                        Modifier.fillMaxWidth().semantics {
                            if (inputError) error(nameErrorMessage)
                        },
                    placeholder = { Text(stringResource(Res.string.instructor_facility_delete_placeholder)) },
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
                        text = stringResource(Res.string.instructor_facility_delete_name_error),
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                        style = appTypography().bodySmall,
                        color = InsColors.Red,
                    )
                }
                if (state is FacilityDeleteState.Failed && state.reason != FacilityDeleteError.NAME_MISMATCH) {
                    Text(
                        text = stringResource(Res.string.instructor_facility_delete_failed),
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
                        onClick = { onAction(FacilityDetailAction.CancelDelete) },
                        enabled = !isSubmitting,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(Res.string.instructor_facility_delete_cancel))
                    }
                    Button(
                        onClick = { onAction(FacilityDetailAction.ConfirmDelete) },
                        enabled = !isSubmitting && typedName == facilityName,
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
                            Text(stringResource(Res.string.instructor_facility_delete_confirm))
                        }
                    }
                }
                if (isSubmitting) {
                    Text(
                        text = stringResource(Res.string.instructor_facility_delete_submitting),
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
private fun FacilityDetailImage(
    reference: String?,
    modifier: Modifier = Modifier,
) {
    val imageModifier = modifier.clip(AppShape.Card)
    if (reference.isNullOrBlank()) {
        FacilityDetailImageFallback(imageModifier)
    } else {
        SubcomposeAsyncImage(
            model = reference,
            contentDescription = null,
            modifier = imageModifier,
            loading = { FacilityDetailImageFallback(Modifier.fillMaxSize()) },
            error = { FacilityDetailImageFallback(Modifier.fillMaxSize()) },
        )
    }
}

@Composable
private fun FacilityDetailImageFallback(modifier: Modifier = Modifier) {
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
private fun FacilityDetailStatus(
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
private fun FacilityDetailError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(AppSpacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.instructor_facility_detail_error),
            style = appTypography().bodyLarge,
            color = InsColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(Res.string.instructor_facility_detail_retry))
        }
    }
}

private fun formatFacilityPhone(value: String): String {
    val digits = value.filter(Char::isDigit)
    return when {
        digits.length == 11 -> "${digits.take(3)}-${digits.substring(3, 7)}-${digits.takeLast(4)}"
        digits.length == 10 -> "${digits.take(3)}-${digits.substring(3, 6)}-${digits.takeLast(4)}"
        else -> value.ifBlank { "-" }
    }
}

private val facilityDetailFixture =
    ManagedFacility(
        id = InstructorFacilityId("facility-preview"),
        name = "클래스잇다 스튜디오",
        address = "서울특별시 강남구 테헤란로",
        detailAddress = "5층 501호",
        phoneNumber = "0212345678",
        description = "회원들이 편하게 운동할 수 있도록 운영하는 시설입니다.",
        openingTime = "09:00",
        closingTime = "22:00",
    )

@Preview(name = "Facility detail", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun FacilityDetailScreenPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        FacilityDetailScreen(
            uiState = FacilityDetailUiState.Content(facilityDetailFixture),
            onAction = {},
        )
    }
}

@Preview(name = "Delete confirmation", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun FacilityDetailScreenPreview_DeleteConfirmation() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        FacilityDetailScreen(
            uiState =
                FacilityDetailUiState.Content(
                    facility = facilityDetailFixture,
                    deleteState = FacilityDeleteState.Confirming(),
                ),
            onAction = {},
        )
    }
}
