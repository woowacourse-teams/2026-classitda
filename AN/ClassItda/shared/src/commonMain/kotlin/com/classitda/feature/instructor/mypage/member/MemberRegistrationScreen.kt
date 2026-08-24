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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_back
import classitda.shared.generated.resources.instructor_member_registration_back
import classitda.shared.generated.resources.instructor_member_registration_intro_description
import classitda.shared.generated.resources.instructor_member_registration_intro_title
import classitda.shared.generated.resources.instructor_member_registration_name
import classitda.shared.generated.resources.instructor_member_registration_name_error
import classitda.shared.generated.resources.instructor_member_registration_name_placeholder
import classitda.shared.generated.resources.instructor_member_registration_phone
import classitda.shared.generated.resources.instructor_member_registration_phone_error
import classitda.shared.generated.resources.instructor_member_registration_phone_placeholder
import classitda.shared.generated.resources.instructor_member_registration_register
import classitda.shared.generated.resources.instructor_member_registration_success
import classitda.shared.generated.resources.instructor_member_registration_success_confirm
import classitda.shared.generated.resources.instructor_member_registration_success_title
import classitda.shared.generated.resources.instructor_member_registration_title
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.instructor.mypage.contract.MemberInputUiModel
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationAction
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationField
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationUiState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MemberRegistrationScreen(
    uiState: MemberRegistrationUiState,
    onAction: (MemberRegistrationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val canAttemptRegistration = uiState is MemberRegistrationUiState.Editing
    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        topBar = {
            MemberRegistrationTopBar(
                onBack = { onAction(MemberRegistrationAction.Back) },
            )
        },
        bottomBar = {
            MemberRegistrationBottomBar(
                enabled = canAttemptRegistration,
                onRegister = { onAction(MemberRegistrationAction.OpenConfirmation) },
                modifier = Modifier.navigationBarsPadding().imePadding(),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
        ) {
            when (uiState) {
                is MemberRegistrationUiState.Editing -> {
                    MemberRegistrationForm(
                        draft = uiState.draft,
                        fieldErrors = uiState.fieldErrors,
                        errorMessage = null,
                        onAction = onAction,
                    )
                }

                is MemberRegistrationUiState.Error -> {
                    MemberRegistrationForm(
                        draft = uiState.draft,
                        fieldErrors = emptySet(),
                        errorMessage = null,
                        onAction = onAction,
                    )
                }

                is MemberRegistrationUiState.Confirmation -> {
                    MemberRegistrationForm(
                        draft = uiState.draft,
                        fieldErrors = emptySet(),
                        errorMessage = null,
                        onAction = onAction,
                    )
                }

                is MemberRegistrationUiState.Submitting -> {
                    MemberRegistrationForm(
                        draft = uiState.draft,
                        fieldErrors = emptySet(),
                        errorMessage = null,
                        onAction = onAction,
                    )
                }

                is MemberRegistrationUiState.Success -> {
                    MemberRegistrationStatusContent(
                        message = stringResource(Res.string.instructor_member_registration_success),
                    )
                }
            }
            MemberRegistrationConfirmDialog(
                state = uiState,
                onAction = onAction,
            )
            if (uiState is MemberRegistrationUiState.Success) {
                MemberRegistrationSuccessDialog(
                    onComplete = {
                        val memberId = uiState.memberId
                        onAction(MemberRegistrationAction.SuccessAcknowledged(memberId))
                    },
                )
            }
        }
    }
}

@Composable
private fun MemberRegistrationSuccessDialog(onComplete: () -> Unit) {
    Dialog(
        onDismissRequest = onComplete,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xxxl),
            shape = AppShape.Card,
            color = InsColors.Surface,
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                Text(
                    text = stringResource(Res.string.instructor_member_registration_success_title),
                    style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = InsColors.TextPrimary,
                )
                Text(
                    text = stringResource(Res.string.instructor_member_registration_success),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                    style = appTypography().bodyLarge,
                    color = InsColors.TextSecondary,
                )
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShape.Card,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = InsColors.Primary,
                            contentColor = InsColors.White,
                        ),
                ) {
                    Text(
                        text = stringResource(Res.string.instructor_member_registration_success_confirm),
                        style = appTypography().bodyLarge.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }
        }
    }
}

@Composable
private fun MemberRegistrationTopBar(onBack: () -> Unit) {
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
                contentDescription = stringResource(Res.string.instructor_member_registration_back),
                tint = InsColors.TextPrimary,
            )
        }
        Text(
            text = stringResource(Res.string.instructor_member_registration_title),
            modifier = Modifier.semantics { heading() },
            style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
        )
    }
}

@Composable
private fun MemberRegistrationForm(
    draft: MemberInputUiModel,
    fieldErrors: Set<MemberRegistrationField>,
    errorMessage: String?,
    onAction: (MemberRegistrationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val nameError = MemberRegistrationField.NAME in fieldErrors
    val phoneError = MemberRegistrationField.PHONE_NUMBER in fieldErrors
    val nameErrorText = stringResource(Res.string.instructor_member_registration_name_error)
    val phoneErrorText = stringResource(Res.string.instructor_member_registration_phone_error)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(
                    start = AppSpacing.screenPadding,
                    top = AppSpacing.xxxl,
                    end = AppSpacing.screenPadding,
                    bottom = AppSpacing.sectionGap,
                ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xxl),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                text = stringResource(Res.string.instructor_member_registration_intro_title),
                style = appTypography().headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = InsColors.TextPrimary,
            )
            Text(
                text = stringResource(Res.string.instructor_member_registration_intro_description),
                style = appTypography().bodyLarge,
                color = InsColors.TextSecondary,
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShape.Card,
            color = InsColors.Surface,
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                RegistrationFieldLabel(
                    text = stringResource(Res.string.instructor_member_registration_name),
                )
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { onAction(MemberRegistrationAction.NameChanged(it)) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .semantics {
                                if (nameError) {
                                    error(nameErrorText)
                                }
                            },
                    placeholder = {
                        Text(
                            text = stringResource(Res.string.instructor_member_registration_name_placeholder),
                            color = InsColors.TextTertiary,
                        )
                    },
                    textStyle = appTypography().bodyLarge.copy(color = InsColors.TextPrimary),
                    singleLine = true,
                    isError = nameError,
                    shape = AppShape.Card,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions =
                        KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        ),
                    colors = registrationFieldColors(),
                )
                if (nameError) {
                    RegistrationFieldError(text = nameErrorText)
                }
                RegistrationFieldLabel(
                    text = stringResource(Res.string.instructor_member_registration_phone),
                )
                OutlinedTextField(
                    value = draft.phoneNumber,
                    onValueChange = { onAction(MemberRegistrationAction.PhoneNumberChanged(it)) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .semantics {
                                if (phoneError) {
                                    error(phoneErrorText)
                                }
                            },
                    placeholder = {
                        Text(
                            text = stringResource(Res.string.instructor_member_registration_phone_placeholder),
                            color = InsColors.TextTertiary,
                        )
                    },
                    textStyle = appTypography().bodyLarge.copy(color = InsColors.TextPrimary),
                    singleLine = true,
                    isError = phoneError,
                    shape = AppShape.Card,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done,
                        ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = registrationFieldColors(),
                )
                if (phoneError) {
                    RegistrationFieldError(text = phoneErrorText)
                }
                errorMessage?.let { message ->
                    Text(
                        text = message,
                        style = appTypography().bodySmall,
                        color = InsColors.Red,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                    )
                }
            }
        }
    }
}

@Composable
private fun RegistrationFieldLabel(text: String) {
    Text(
        text = text,
        style = appTypography().titleMedium.copy(fontWeight = FontWeight.Bold),
        color = InsColors.TextPrimary,
    )
}

@Composable
private fun RegistrationFieldError(text: String) {
    Text(
        text = text,
        style = appTypography().bodySmall,
        color = InsColors.Red,
    )
}

@Composable
private fun registrationFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = InsColors.SurfaceVariant,
        unfocusedContainerColor = InsColors.SurfaceVariant,
        focusedBorderColor = InsColors.TextPrimary,
        unfocusedBorderColor = InsColors.Divider,
        errorBorderColor = InsColors.Red,
        cursorColor = InsColors.TextPrimary,
        errorCursorColor = InsColors.Red,
    )

@Composable
private fun MemberRegistrationBottomBar(
    enabled: Boolean,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = InsColors.Background,
    ) {
        Button(
            onClick = onRegister,
            enabled = enabled,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.lg),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = InsColors.Primary,
                    contentColor = InsColors.White,
                    disabledContainerColor = InsColors.SurfaceVariant,
                    disabledContentColor = InsColors.TextTertiary,
                ),
            shape = AppShape.Card,
        ) {
            Text(
                text = stringResource(Res.string.instructor_member_registration_register),
                style = appTypography().bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun MemberRegistrationStatusContent(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .semantics { liveRegion = LiveRegionMode.Polite },
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

private val memberRegistrationEmptyState =
    MemberRegistrationUiState.Editing(
        draft = MemberInputUiModel(),
        canSubmit = false,
    )

private val memberRegistrationInputState =
    MemberRegistrationUiState.Editing(
        draft = MemberInputUiModel(name = "김민지", phoneNumber = "01012345678"),
        canSubmit = true,
    )

private val memberRegistrationErrorState =
    MemberRegistrationUiState.Editing(
        draft = MemberInputUiModel(name = "", phoneNumber = "010"),
        canSubmit = false,
        fieldErrors = setOf(MemberRegistrationField.NAME, MemberRegistrationField.PHONE_NUMBER),
    )

@Preview(
    name = "Default · Instructor",
    group = "Screen/MemberRegistration",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MemberRegistrationScreenPreview_Default() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        MemberRegistrationScreen(
            uiState = memberRegistrationEmptyState,
            onAction = {},
        )
    }
}

@Preview(
    name = "Input · Instructor",
    group = "Screen/MemberRegistration",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MemberRegistrationScreenPreview_Input() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        MemberRegistrationScreen(
            uiState = memberRegistrationInputState,
            onAction = {},
        )
    }
}

@Preview(
    name = "Field errors · Instructor",
    group = "Screen/MemberRegistration",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MemberRegistrationScreenPreview_FieldErrors() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        MemberRegistrationScreen(
            uiState = memberRegistrationErrorState,
            onAction = {},
        )
    }
}

@Preview(
    name = "Keyboard boundary · Instructor",
    group = "Screen/MemberRegistration",
    widthDp = 320,
    heightDp = 520,
    fontScale = 1.5f,
)
@Composable
private fun MemberRegistrationScreenPreview_KeyboardBoundary() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        MemberRegistrationScreen(
            uiState = memberRegistrationInputState,
            onAction = {},
        )
    }
}
