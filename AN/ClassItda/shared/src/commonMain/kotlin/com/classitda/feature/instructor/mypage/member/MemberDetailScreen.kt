package com.classitda.feature.instructor.mypage.member

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_back
import classitda.shared.generated.resources.ic_person
import classitda.shared.generated.resources.instructor_member_delete_cancel
import classitda.shared.generated.resources.instructor_member_delete_confirm
import classitda.shared.generated.resources.instructor_member_delete_failed
import classitda.shared.generated.resources.instructor_member_delete_message
import classitda.shared.generated.resources.instructor_member_delete_name_error
import classitda.shared.generated.resources.instructor_member_delete_pane_title
import classitda.shared.generated.resources.instructor_member_delete_placeholder
import classitda.shared.generated.resources.instructor_member_delete_submitting
import classitda.shared.generated.resources.instructor_member_delete_title
import classitda.shared.generated.resources.instructor_member_detail_back
import classitda.shared.generated.resources.instructor_member_detail_delete
import classitda.shared.generated.resources.instructor_member_detail_edit
import classitda.shared.generated.resources.instructor_member_detail_error
import classitda.shared.generated.resources.instructor_member_detail_information
import classitda.shared.generated.resources.instructor_member_detail_loading
import classitda.shared.generated.resources.instructor_member_detail_name
import classitda.shared.generated.resources.instructor_member_detail_phone
import classitda.shared.generated.resources.instructor_member_detail_retry
import classitda.shared.generated.resources.instructor_member_detail_title
import coil3.compose.SubcomposeAsyncImage
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.ManagedMember
import com.classitda.feature.instructor.mypage.contract.MemberDeleteError
import com.classitda.feature.instructor.mypage.contract.MemberDeleteState
import com.classitda.feature.instructor.mypage.contract.MemberDetailAction
import com.classitda.feature.instructor.mypage.contract.MemberDetailUiState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MemberDetailScreen(
    uiState: MemberDetailUiState,
    onAction: (MemberDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = uiState as? MemberDetailUiState.Content
    val isDeleting = content?.deleteState is MemberDeleteState.Submitting
    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { if (!isDeleting) onAction(MemberDetailAction.Back) }) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_back),
                        contentDescription = stringResource(Res.string.instructor_member_detail_back),
                        tint = InsColors.TextPrimary,
                    )
                }
                Text(
                    text = stringResource(Res.string.instructor_member_detail_title),
                    modifier = Modifier.weight(1f).semantics { heading() },
                    style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = InsColors.TextPrimary,
                )
                Box(modifier = Modifier.size(AppSpacing.xxxl))
            }
        },
        bottomBar = {
            if (content != null && !isDeleting) {
                Row(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(AppSpacing.screenPadding),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    TextButton(
                        onClick = { onAction(MemberDetailAction.RequestDelete) },
                        modifier = Modifier.weight(1f).heightIn(min = AppSpacing.xxxl + AppSpacing.lg),
                    ) {
                        Text(
                            text = stringResource(Res.string.instructor_member_detail_delete),
                            color = InsColors.Red,
                            style = appTypography().bodyLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                    Button(
                        onClick = { onAction(MemberDetailAction.OpenEdit) },
                        modifier = Modifier.weight(1f).heightIn(min = AppSpacing.xxxl + AppSpacing.lg),
                        shape = AppShape.Card,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = InsColors.Primary,
                                contentColor = InsColors.White,
                            ),
                    ) {
                        Text(
                            text = stringResource(Res.string.instructor_member_detail_edit),
                            style = appTypography().bodyLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        when (uiState) {
            MemberDetailUiState.Loading -> {
                MemberDetailStatus(
                    stringResource(Res.string.instructor_member_detail_loading),
                    Modifier.padding(innerPadding),
                )
            }

            is MemberDetailUiState.Error -> {
                MemberDetailError(
                    onRetry = { onAction(MemberDetailAction.Retry) },
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is MemberDetailUiState.Content -> {
                MemberDetailContent(
                    member = uiState.member,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is MemberDetailUiState.Deleted -> {
                MemberDetailStatus(
                    stringResource(Res.string.instructor_member_delete_submitting),
                    Modifier.padding(innerPadding),
                )
            }
        }
    }
    if (content != null) {
        MemberDeleteDialog(content.member.name, content.deleteState, onAction)
    }
}

@Composable
private fun MemberDetailContent(
    member: ManagedMember,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(AppSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MemberAvatar(member, Modifier.size(AppSpacing.xxxl * 3))
        Text(
            text = member.name,
            style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
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
                    text = stringResource(Res.string.instructor_member_detail_information),
                    style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = InsColors.TextPrimary,
                )
                MemberDetailRow(
                    stringResource(Res.string.instructor_member_detail_name),
                    member.name,
                )
                MemberDetailRow(
                    stringResource(Res.string.instructor_member_detail_phone),
                    formatMemberPhone(member.phoneNumber),
                )
            }
        }
    }
}

@Composable
private fun MemberDetailRow(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(label, style = appTypography().labelLarge, color = InsColors.TextSecondary)
        Text(value, style = appTypography().bodyLarge, color = InsColors.TextPrimary)
    }
}

@Composable
private fun MemberAvatar(
    member: ManagedMember,
    modifier: Modifier = Modifier,
) {
    val fallback = @Composable {
        Box(
            modifier = modifier.clip(CircleShape).background(InsColors.Gray200),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_person),
                contentDescription = null,
                tint = InsColors.TextTertiary,
                modifier = Modifier.size(AppSpacing.xxxl),
            )
        }
    }
    if (member.profileImageUrl.isNullOrBlank()) {
        fallback()
    } else {
        SubcomposeAsyncImage(
            model = member.profileImageUrl,
            contentDescription = null,
            modifier = modifier.clip(CircleShape),
            loading = { fallback() },
            error = { fallback() },
        )
    }
}

@Composable
private fun MemberDeleteDialog(
    memberName: String,
    state: MemberDeleteState,
    onAction: (MemberDetailAction) -> Unit,
) {
    if (state == MemberDeleteState.Hidden) return
    val isSubmitting = state is MemberDeleteState.Submitting
    val typedName =
        when (state) {
            is MemberDeleteState.Confirming -> state.typedName
            is MemberDeleteState.Failed -> state.typedName
            else -> ""
        }
    val inputError =
        (typedName.isNotBlank() && typedName != memberName) ||
            when (state) {
                is MemberDeleteState.Confirming -> state.error == MemberDeleteError.NAME_MISMATCH
                is MemberDeleteState.Failed -> state.reason == MemberDeleteError.NAME_MISMATCH
                else -> false
            }
    val errorText = stringResource(Res.string.instructor_member_delete_name_error)
    val deletePaneTitle = stringResource(Res.string.instructor_member_delete_pane_title)
    Dialog(
        onDismissRequest = { if (!isSubmitting) onAction(MemberDetailAction.CancelDelete) },
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
                    paneTitle = deletePaneTitle
                },
            shape = AppShape.Card,
            color = InsColors.Surface,
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                Text(
                    text = stringResource(Res.string.instructor_member_delete_title),
                    style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = InsColors.TextPrimary,
                )
                Text(
                    text = stringResource(Res.string.instructor_member_delete_message),
                    style = appTypography().bodyMedium,
                    color = InsColors.TextSecondary,
                )
                Text(
                    text = memberName,
                    style = appTypography().titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = InsColors.TextPrimary,
                )
                OutlinedTextField(
                    value = typedName,
                    onValueChange = { onAction(MemberDetailAction.DeleteNameChanged(it)) },
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth().semantics { if (inputError) error(errorText) },
                    placeholder = { Text(stringResource(Res.string.instructor_member_delete_placeholder)) },
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
                        text = errorText,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                        style = appTypography().bodySmall,
                        color = InsColors.Red,
                    )
                }
                if (state is MemberDeleteState.Failed && state.reason != MemberDeleteError.NAME_MISMATCH) {
                    Text(
                        text = stringResource(Res.string.instructor_member_delete_failed),
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
                        onClick = { onAction(MemberDetailAction.CancelDelete) },
                        enabled = !isSubmitting,
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(Res.string.instructor_member_delete_cancel)) }
                    Button(
                        onClick = { onAction(MemberDetailAction.ConfirmDelete) },
                        enabled = !isSubmitting && typedName == memberName,
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
                            Text(stringResource(Res.string.instructor_member_delete_confirm))
                        }
                    }
                }
                if (isSubmitting) {
                    Text(
                        text = stringResource(Res.string.instructor_member_delete_submitting),
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
private fun MemberDetailStatus(
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
private fun MemberDetailError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(AppSpacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.instructor_member_detail_error),
            style = appTypography().bodyLarge,
            color = InsColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(Res.string.instructor_member_detail_retry))
        }
    }
}

private fun formatMemberPhone(value: String): String {
    val digits = value.filter(Char::isDigit)
    return when {
        digits.length == 11 -> "${digits.take(3)}-${digits.substring(3, 7)}-${digits.takeLast(4)}"
        digits.length == 10 -> "${digits.take(3)}-${digits.substring(3, 6)}-${digits.takeLast(4)}"
        else -> value.ifBlank { "-" }
    }
}

private val memberDetailFixture =
    ManagedMember(
        id = InstructorMemberId("member-detail-preview"),
        name = "김민지",
        phoneNumber = "01012345678",
    )

@Preview(name = "Member detail", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun MemberDetailScreenPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        MemberDetailScreen(MemberDetailUiState.Content(memberDetailFixture), onAction = {})
    }
}
