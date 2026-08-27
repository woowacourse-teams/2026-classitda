package com.classitda.feature.instructor.mypage.member

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import classitda.shared.generated.resources.ic_close
import classitda.shared.generated.resources.instructor_member_detail_loading
import classitda.shared.generated.resources.instructor_member_detail_retry
import classitda.shared.generated.resources.instructor_member_edit_back
import classitda.shared.generated.resources.instructor_member_edit_error
import classitda.shared.generated.resources.instructor_member_edit_intro_description
import classitda.shared.generated.resources.instructor_member_edit_intro_title
import classitda.shared.generated.resources.instructor_member_edit_save
import classitda.shared.generated.resources.instructor_member_edit_submitting
import classitda.shared.generated.resources.instructor_member_edit_success
import classitda.shared.generated.resources.instructor_member_edit_success_title
import classitda.shared.generated.resources.instructor_member_edit_title
import classitda.shared.generated.resources.instructor_member_registration_card_title
import classitda.shared.generated.resources.instructor_member_registration_name
import classitda.shared.generated.resources.instructor_member_registration_name_error
import classitda.shared.generated.resources.instructor_member_registration_name_placeholder
import classitda.shared.generated.resources.instructor_member_registration_phone
import classitda.shared.generated.resources.instructor_member_registration_phone_error
import classitda.shared.generated.resources.instructor_member_registration_phone_placeholder
import classitda.shared.generated.resources.instructor_member_registration_required
import classitda.shared.generated.resources.instructor_member_registration_success_confirm
import classitda.shared.generated.resources.phone_number_change_close
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.feature.instructor.mypage.InstructorPhoneNumberVisualTransformation
import com.classitda.feature.instructor.mypage.contract.MemberEditAction
import com.classitda.feature.instructor.mypage.contract.MemberEditField
import com.classitda.feature.instructor.mypage.contract.MemberEditUiState
import com.classitda.feature.instructor.mypage.contract.MemberInputUiModel
import com.classitda.feature.instructor.mypage.instructorPhoneNumberDigits
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MemberEditScreen(
    uiState: MemberEditUiState,
    onAction: (MemberEditAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var successDialogVisible by remember { mutableStateOf(false) }
    var successDialogPresented by remember { mutableStateOf(false) }
    LaunchedEffect(uiState) {
        if (uiState is MemberEditUiState.Success) {
            successDialogPresented = true
            successDialogVisible = true
        }
    }
    LaunchedEffect(successDialogVisible) {
        val success = uiState as? MemberEditUiState.Success
        if (successDialogPresented && !successDialogVisible && success != null) {
            onAction(MemberEditAction.SuccessAcknowledged(success.memberId))
        }
    }
    val editing = uiState as? MemberEditUiState.Editing
    val draft =
        when (uiState) {
            is MemberEditUiState.Editing -> uiState.draft
            is MemberEditUiState.Submitting -> uiState.draft
            is MemberEditUiState.Error -> uiState.draft
            else -> MemberInputUiModel()
        }
    val errors = editing?.fieldErrors.orEmpty()
    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { onAction(MemberEditAction.Back) }) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_back),
                        contentDescription = stringResource(Res.string.instructor_member_edit_back),
                        tint = InsColors.TextPrimary,
                    )
                }
                Text(
                    text = stringResource(Res.string.instructor_member_edit_title),
                    modifier = Modifier.weight(1f).semantics { heading() },
                    style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = InsColors.TextPrimary,
                )
                Box(modifier = Modifier.padding(AppSpacing.xxl))
            }
        },
        bottomBar = {
            Button(
                onClick = { onAction(MemberEditAction.Submit) },
                enabled = editing?.canSubmit == true,
                modifier =
                    Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(
                        AppSpacing.screenPadding,
                    ),
                shape = AppShape.Card,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = InsColors.Primary,
                        contentColor = InsColors.White,
                        disabledContainerColor = InsColors.Gray300,
                        disabledContentColor = InsColors.TextTertiary,
                    ),
            ) {
                if (uiState is MemberEditUiState.Submitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(vertical = AppSpacing.xs),
                        color = InsColors.White,
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.instructor_member_edit_save),
                        style = appTypography().bodyLarge.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }
        },
    ) { innerPadding ->
        when (uiState) {
            MemberEditUiState.Loading -> {
                MemberEditStatus(
                    stringResource(Res.string.instructor_member_detail_loading),
                    Modifier.padding(innerPadding),
                )
            }

            is MemberEditUiState.Error -> {
                MemberEditError(
                    onRetry = { onAction(MemberEditAction.Retry) },
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is MemberEditUiState.Success -> {
                MemberEditStatus(
                    stringResource(Res.string.instructor_member_edit_success),
                    Modifier.padding(innerPadding),
                )
            }

            else -> {
                MemberEditForm(draft, errors, onAction, Modifier.padding(innerPadding))
            }
        }
    }
    if (uiState is MemberEditUiState.Success && successDialogVisible) {
        MemberEditSuccessDialog(
            onClose = {
                successDialogVisible = false
            },
        )
    }
}

@Composable
private fun MemberEditSuccessDialog(onClose: () -> Unit) {
    val paneTitle = stringResource(Res.string.instructor_member_edit_success_title)
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
                    text = stringResource(Res.string.instructor_member_edit_success),
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
private fun MemberEditForm(
    draft: MemberInputUiModel,
    errors: Set<MemberEditField>,
    onAction: (MemberEditAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val nameError = MemberEditField.NAME in errors
    val phoneError = MemberEditField.PHONE_NUMBER in errors
    val nameErrorText = stringResource(Res.string.instructor_member_registration_name_error)
    val phoneErrorText = stringResource(Res.string.instructor_member_registration_phone_error)
    Column(
        modifier =
            modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(
                horizontal = AppSpacing.screenPadding,
                vertical = AppSpacing.xxxl,
            ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xxl),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                text = stringResource(Res.string.instructor_member_edit_intro_title),
                style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = InsColors.TextPrimary,
            )
            Text(
                text = stringResource(Res.string.instructor_member_edit_intro_description),
                style = appTypography().bodyMedium,
                color = InsColors.TextSecondary,
            )
        }
        Surface(shape = AppShape.Card, color = InsColors.Surface, modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(AppSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = stringResource(Res.string.instructor_member_registration_card_title),
                        style = appTypography().titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = InsColors.TextPrimary,
                    )
                    Text(
                        text = stringResource(Res.string.instructor_member_registration_required),
                        style = appTypography().labelMedium,
                        color = InsColors.TextSecondary,
                    )
                }
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { onAction(MemberEditAction.NameChanged(it)) },
                    modifier = Modifier.fillMaxWidth().semantics { if (nameError) error(nameErrorText) },
                    label = { Text(stringResource(Res.string.instructor_member_registration_name)) },
                    placeholder = { Text(stringResource(Res.string.instructor_member_registration_name_placeholder)) },
                    isError = nameError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = memberEditFieldColors(),
                )
                if (nameError) {
                    Text(nameErrorText, style = appTypography().bodySmall, color = InsColors.Red)
                }
                OutlinedTextField(
                    value = instructorPhoneNumberDigits(draft.phoneNumber),
                    onValueChange = {
                        onAction(MemberEditAction.PhoneNumberChanged(instructorPhoneNumberDigits(it)))
                    },
                    modifier = Modifier.fillMaxWidth().semantics { if (phoneError) error(phoneErrorText) },
                    enabled = draft.phoneNumberEditable,
                    label = { Text(stringResource(Res.string.instructor_member_registration_phone)) },
                    placeholder = { Text(stringResource(Res.string.instructor_member_registration_phone_placeholder)) },
                    isError = phoneError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    visualTransformation = InstructorPhoneNumberVisualTransformation,
                    colors = memberEditFieldColors(),
                )
                if (phoneError) {
                    Text(phoneErrorText, style = appTypography().bodySmall, color = InsColors.Red)
                }
            }
        }
    }
}

@Composable
private fun memberEditFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = InsColors.Primary,
        unfocusedBorderColor = InsColors.Divider,
        errorBorderColor = InsColors.Red,
    )

@Composable
private fun MemberEditStatus(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = InsColors.Primary)
        Text(message, modifier = Modifier.padding(top = AppSpacing.lg), color = InsColors.TextSecondary)
    }
}

@Composable
private fun MemberEditError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(AppSpacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.instructor_member_edit_error),
            color = InsColors.TextSecondary,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
        )
        TextButton(onClick = onRetry) { Text(stringResource(Res.string.instructor_member_detail_retry)) }
    }
}

@Preview(name = "Member edit", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun MemberEditScreenPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        MemberEditScreen(
            MemberEditUiState.Editing(
                memberId = InstructorMemberId("member-edit-preview"),
                draft = MemberInputUiModel("김민지", "01012345678"),
                canSubmit = true,
            ),
            onAction = {},
        )
    }
}
