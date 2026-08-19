package com.classitda.feature.student.mypage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_back
import classitda.shared.generated.resources.ic_camera
import classitda.shared.generated.resources.ic_edit
import classitda.shared.generated.resources.profile_edit_back
import classitda.shared.generated.resources.profile_edit_complete
import classitda.shared.generated.resources.profile_edit_email
import classitda.shared.generated.resources.profile_edit_error_description
import classitda.shared.generated.resources.profile_edit_error_title
import classitda.shared.generated.resources.profile_edit_loading
import classitda.shared.generated.resources.profile_edit_name
import classitda.shared.generated.resources.profile_edit_name_input
import classitda.shared.generated.resources.profile_edit_phone_number
import classitda.shared.generated.resources.profile_edit_phone_number_change
import classitda.shared.generated.resources.profile_edit_photo_change
import classitda.shared.generated.resources.profile_edit_retry
import classitda.shared.generated.resources.profile_edit_save_failed
import classitda.shared.generated.resources.profile_edit_saving
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.student.mypage.MemberId
import com.classitda.domain.model.student.mypage.MemberProfile
import com.classitda.domain.repository.student.mypage.MyPageFailureReason
import com.classitda.feature.student.mypage.contract.ProfileEditAction
import com.classitda.feature.student.mypage.contract.ProfileEditUiState
import com.classitda.feature.student.mypage.preview.MyPageProfileBoundaryFixture
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileEditScreen(
    uiState: ProfileEditUiState,
    onAction: (ProfileEditAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSaving = uiState is ProfileEditUiState.Saving
    val canSave =
        when (uiState) {
            is ProfileEditUiState.Editing -> uiState.canSave
            is ProfileEditUiState.SaveFailed -> true
            else -> false
        }

    Scaffold(
        modifier = modifier,
        containerColor = StuColors.Background,
        topBar = {
            ProfileEditTopBar(
                isSaveEnabled = canSave && !isSaving,
                isSaving = isSaving,
                onBack = { onAction(ProfileEditAction.Back) },
                onSave = { onAction(ProfileEditAction.Save) },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            ProfileEditUiState.Loading -> {
                ProfileEditLoadingContent(
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is ProfileEditUiState.Editing -> {
                ProfileEditContent(
                    profile = uiState.profile,
                    draftName = uiState.draftName,
                    isSaving = false,
                    saveFailureMessage = null,
                    onAction = onAction,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is ProfileEditUiState.Saving -> {
                ProfileEditContent(
                    profile = uiState.profile,
                    draftName = uiState.draftName,
                    isSaving = true,
                    saveFailureMessage = null,
                    onAction = onAction,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is ProfileEditUiState.SaveFailed -> {
                ProfileEditContent(
                    profile = uiState.profile,
                    draftName = uiState.draftName,
                    isSaving = false,
                    saveFailureMessage = stringResource(Res.string.profile_edit_save_failed),
                    onAction = onAction,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is ProfileEditUiState.Error -> {
                ProfileEditErrorContent(
                    onRetry = { onAction(ProfileEditAction.Retry) },
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun ProfileEditTopBar(
    isSaveEnabled: Boolean,
    isSaving: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    val typography = appTypography()

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_back),
                contentDescription = stringResource(Res.string.profile_edit_back),
                modifier = Modifier.size(AppSpacing.xxl),
                tint = StuColors.TextPrimary,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        TextButton(
            enabled = isSaveEnabled,
            onClick = onSave,
            colors =
                ButtonDefaults.textButtonColors(
                    contentColor = StuColors.TextPrimary,
                    disabledContentColor = StuColors.TextTertiary,
                ),
        ) {
            Text(
                text =
                    if (isSaving) {
                        stringResource(Res.string.profile_edit_saving)
                    } else {
                        stringResource(Res.string.profile_edit_complete)
                    },
                style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun ProfileEditContent(
    profile: MemberProfile,
    draftName: String,
    isSaving: Boolean,
    saveFailureMessage: String?,
    onAction: (ProfileEditAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val typography = appTypography()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = AppSpacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        EditableProfileAvatar(
            name = profile.name,
            enabled = !isSaving,
            onClick = { onAction(ProfileEditAction.RequestPhotoChange) },
        )
        Spacer(modifier = Modifier.height(AppSpacing.xxxl * 2))
        EditableNameField(
            value = draftName,
            enabled = !isSaving,
            onValueChange = { onAction(ProfileEditAction.NameChanged(it)) },
            onDone = { keyboardController?.hide() },
        )
        Spacer(modifier = Modifier.height(AppSpacing.xl))
        PhoneNumberField(
            phoneNumber = profile.phoneNumber,
            enabled = !isSaving,
            onChange = { onAction(ProfileEditAction.OpenPhoneNumberChange) },
        )
        Spacer(modifier = Modifier.height(AppSpacing.xl))
        ReadOnlyEmailField(email = profile.email)
        if (saveFailureMessage != null) {
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            Text(
                text = saveFailureMessage,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .semantics { error(saveFailureMessage) },
                style = typography.bodyMedium,
                color = StuColors.Red,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.xxl))
    }
}

@Composable
private fun EditableProfileAvatar(
    name: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val photoChangeDescription = stringResource(Res.string.profile_edit_photo_change)
    val typography = appTypography()

    Box(
        modifier =
            Modifier
                .size(AppSpacing.xxxl * 3)
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                ).semantics(mergeDescendants = true) {
                    contentDescription = photoChangeDescription
                },
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        color = StuColors.SurfaceVariant,
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name.first { !it.isWhitespace() }.toString(),
                style = typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = StuColors.TextPrimary,
            )
        }
        Surface(
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = AppSpacing.sm, y = AppSpacing.sm)
                    .size(AppSpacing.xxxl),
            shape = CircleShape,
            color = StuColors.Surface,
            border =
                BorderStroke(
                    width = AppSpacing.xs / 4,
                    color = StuColors.Divider,
                ),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(Res.drawable.ic_camera),
                    contentDescription = null,
                    modifier = Modifier.size(AppSpacing.xl),
                    tint = StuColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun EditableNameField(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    val inputDescription = stringResource(Res.string.profile_edit_name_input)
    val typography = appTypography()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        ProfileEditFieldLabel(text = stringResource(Res.string.profile_edit_name))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = inputDescription },
            enabled = enabled,
            textStyle = typography.bodyLarge,
            singleLine = true,
            shape = AppShape.Card,
            trailingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_edit),
                    contentDescription = null,
                    modifier = Modifier.size(AppSpacing.xl),
                )
            },
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = StuColors.SurfaceVariant,
                    unfocusedContainerColor = StuColors.SurfaceVariant,
                    disabledContainerColor = StuColors.SurfaceVariant,
                    focusedBorderColor = StuColors.TextPrimary,
                    unfocusedBorderColor = StuColors.Divider,
                    disabledBorderColor = StuColors.Divider,
                    cursorColor = StuColors.TextPrimary,
                ),
        )
    }
}

@Composable
private fun PhoneNumberField(
    phoneNumber: String,
    enabled: Boolean,
    onChange: () -> Unit,
) {
    val typography = appTypography()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        ProfileEditFieldLabel(text = stringResource(Res.string.profile_edit_phone_number))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShape.Card,
            color = StuColors.SurfaceVariant,
            border =
                BorderStroke(
                    width = AppSpacing.xs / 4,
                    color = StuColors.Divider,
                ),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    text = phoneNumber,
                    modifier = Modifier.weight(1f),
                    style = typography.bodyLarge,
                    color = StuColors.TextSecondary,
                )
                Button(
                    enabled = enabled,
                    onClick = onChange,
                    shape = AppShape.Pill,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = StuColors.PrimaryColor,
                            contentColor = StuColors.White,
                        ),
                    contentPadding = ButtonDefaults.ContentPadding,
                ) {
                    Text(
                        text = stringResource(Res.string.profile_edit_phone_number_change),
                        style = typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadOnlyEmailField(email: String) {
    val typography = appTypography()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        ProfileEditFieldLabel(text = stringResource(Res.string.profile_edit_email))
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {},
            shape = AppShape.Card,
            color = StuColors.SurfaceVariant,
            border =
                BorderStroke(
                    width = AppSpacing.xs / 4,
                    color = StuColors.Divider,
                ),
        ) {
            Text(
                text = email,
                modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
                style = typography.bodyLarge,
                color = StuColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun ProfileEditFieldLabel(text: String) {
    val typography = appTypography()

    Text(
        text = text,
        style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = StuColors.TextPrimary,
    )
}

@Composable
private fun ProfileEditLoadingContent(modifier: Modifier = Modifier) {
    val typography = appTypography()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = StuColors.Green)
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        Text(
            text = stringResource(Res.string.profile_edit_loading),
            style = typography.bodyMedium,
            color = StuColors.TextSecondary,
        )
    }
}

@Composable
private fun ProfileEditErrorContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = appTypography()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = AppSpacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.profile_edit_error_title),
            modifier = Modifier.fillMaxWidth(),
            style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        Text(
            text = stringResource(Res.string.profile_edit_error_description),
            modifier = Modifier.fillMaxWidth(),
            style = typography.bodyMedium,
            color = StuColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppSpacing.xxl))
        OutlinedButton(onClick = onRetry) {
            Text(text = stringResource(Res.string.profile_edit_retry))
        }
    }
}

private fun ProfileEditAction.previewLabel(): String =
    when (this) {
        ProfileEditAction.Back -> "Back"
        ProfileEditAction.Retry -> "Retry"
        is ProfileEditAction.NameChanged -> "NameChanged(name=$name)"
        ProfileEditAction.RequestPhotoChange -> "RequestPhotoChange"
        ProfileEditAction.OpenPhoneNumberChange -> "OpenPhoneNumberChange"
        ProfileEditAction.Save -> "Save"
    }

private object ProfileEditPreviewFixture {
    val profile =
        MemberProfile(
            id = MemberId("member-profile-edit-preview"),
            name = "김민지",
            phoneNumber = "010-1234-5678",
            email = "class12345@gmail.com",
            profileImageUrl = null,
        )

    val default =
        ProfileEditUiState.Editing(
            profile = profile,
            draftName = profile.name,
            canSave = false,
        )
    val changed =
        ProfileEditUiState.Editing(
            profile = profile,
            draftName = "김민정",
            canSave = true,
        )
    val unavailable =
        ProfileEditUiState.Editing(
            profile = profile,
            draftName = "",
            canSave = false,
        )
    val saving =
        ProfileEditUiState.Saving(
            profile = profile,
            draftName = "김민정",
        )
    val saveFailed =
        ProfileEditUiState.SaveFailed(
            profile = profile,
            draftName = "김민정",
            reason = MyPageFailureReason.NETWORK,
        )
}

@Preview(
    name = "Default · Student · Save disabled",
    group = "Screen/ProfileEdit",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ProfileEditScreenPreview_Default_Student_SaveDisabled() {
    AppTheme(theme = ThemeType.STUDENT) {
        ProfileEditScreen(
            uiState = ProfileEditPreviewFixture.default,
            onAction = {},
        )
    }
}

@Preview(
    name = "Changed · Student · Save enabled",
    group = "Screen/ProfileEdit",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ProfileEditScreenPreview_Changed_Student_SaveEnabled() {
    AppTheme(theme = ThemeType.STUDENT) {
        ProfileEditScreen(
            uiState = ProfileEditPreviewFixture.changed,
            onAction = {},
        )
    }
}

@Preview(
    name = "Unavailable · Student · Empty name",
    group = "Screen/ProfileEdit",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ProfileEditScreenPreview_Unavailable_Student_EmptyName() {
    AppTheme(theme = ThemeType.STUDENT) {
        ProfileEditScreen(
            uiState = ProfileEditPreviewFixture.unavailable,
            onAction = {},
        )
    }
}

@Preview(
    name = "Saving · Student · Disabled",
    group = "Screen/ProfileEdit",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ProfileEditScreenPreview_Saving_Student_Disabled() {
    AppTheme(theme = ThemeType.STUDENT) {
        ProfileEditScreen(
            uiState = ProfileEditPreviewFixture.saving,
            onAction = {},
        )
    }
}

@Preview(
    name = "Save failed · Student · Message",
    group = "Screen/ProfileEdit",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ProfileEditScreenPreview_SaveFailed_Student_Message() {
    AppTheme(theme = ThemeType.STUDENT) {
        ProfileEditScreen(
            uiState = ProfileEditPreviewFixture.saveFailed,
            onAction = {},
        )
    }
}

@Preview(
    name = "Actions · Student · Interactive",
    group = "Harness/ProfileEdit",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ProfileEditScreenPreview_Actions_Student_Interactive() {
    var draftName by remember { mutableStateOf(ProfileEditPreviewFixture.profile.name) }
    var lastAction by remember { mutableStateOf("None") }

    AppTheme(theme = ThemeType.STUDENT) {
        val typography = appTypography()

        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "마지막 행동: $lastAction",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.sm),
                style = typography.labelLarge,
            )
            ProfileEditScreen(
                uiState =
                    ProfileEditUiState.Editing(
                        profile = ProfileEditPreviewFixture.profile,
                        draftName = draftName,
                        canSave = draftName.isNotBlank() && draftName != ProfileEditPreviewFixture.profile.name,
                    ),
                onAction = { action ->
                    if (action is ProfileEditAction.NameChanged) {
                        draftName = action.name
                    }
                    lastAction = action.previewLabel()
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Preview(
    name = "F04 · Long content · Large font · Small screen",
    group = "Boundary/MyPageProfile",
    widthDp = 320,
    heightDp = 568,
    fontScale = 1.5f,
)
@Composable
private fun ProfileEditScreenPreview_Boundary_LongContent_LargeFont_SmallScreen() {
    AppTheme(theme = ThemeType.STUDENT) {
        ProfileEditScreen(
            uiState = MyPageProfileBoundaryFixture.profileEditState,
            onAction = {},
        )
    }
}
