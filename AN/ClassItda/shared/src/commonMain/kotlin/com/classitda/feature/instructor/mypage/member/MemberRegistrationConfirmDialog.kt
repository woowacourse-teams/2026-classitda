package com.classitda.feature.instructor.mypage.member

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
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
import classitda.shared.generated.resources.instructor_member_registration_confirm_cancel
import classitda.shared.generated.resources.instructor_member_registration_confirm_error
import classitda.shared.generated.resources.instructor_member_registration_confirm_message
import classitda.shared.generated.resources.instructor_member_registration_confirm_pane_title
import classitda.shared.generated.resources.instructor_member_registration_confirm_register
import classitda.shared.generated.resources.instructor_member_registration_confirm_retry
import classitda.shared.generated.resources.instructor_member_registration_confirm_submitting
import classitda.shared.generated.resources.instructor_member_registration_confirm_title
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.instructor.mypage.contract.MemberInputUiModel
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationAction
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationUiError
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationUiState
import org.jetbrains.compose.resources.stringResource

/** F07 is an overlay state of F06; it is not a destination of its own. */
@Composable
internal fun MemberRegistrationConfirmDialog(
    state: MemberRegistrationUiState,
    onAction: (MemberRegistrationAction) -> Unit,
) {
    val dialogState =
        when (state) {
            is MemberRegistrationUiState.Confirmation -> confirmationDialogState(state.draft)
            is MemberRegistrationUiState.Submitting -> SubmittingDialogState(state.draft)
            is MemberRegistrationUiState.Error -> FailedDialogState(state.draft)
            else -> return
        }
    val isSubmitting = dialogState is SubmittingDialogState
    val dialogPaneTitle = stringResource(Res.string.instructor_member_registration_confirm_pane_title)

    Dialog(
        onDismissRequest = {
            if (!isSubmitting) {
                onAction(MemberRegistrationAction.CancelConfirmation)
            }
        },
        properties =
            DialogProperties(
                dismissOnBackPress = !isSubmitting,
                dismissOnClickOutside = !isSubmitting,
                usePlatformDefaultWidth = false,
            ),
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.xxxl)
                    .semantics { paneTitle = dialogPaneTitle },
            shape = AppShape.Card,
            color = InsColors.Surface,
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                Text(
                    text = stringResource(Res.string.instructor_member_registration_confirm_title),
                    style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = InsColors.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                RegistrationAvatar(name = dialogState.draft.name)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    Text(
                        text = dialogState.draft.name,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = appTypography().titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = InsColors.TextPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = dialogState.draft.displayPhoneNumber,
                        style = appTypography().bodyMedium,
                        color = InsColors.TextSecondary,
                    )
                }
                Text(
                    text = stringResource(Res.string.instructor_member_registration_confirm_message),
                    modifier = Modifier.fillMaxWidth(),
                    style = appTypography().bodyMedium,
                    color = InsColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
                if (dialogState is FailedDialogState) {
                    Text(
                        text = stringResource(Res.string.instructor_member_registration_confirm_error),
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                        style = appTypography().bodySmall,
                        color = InsColors.Red,
                        textAlign = TextAlign.Center,
                    )
                }
                RegistrationConfirmationActions(
                    state = dialogState,
                    onAction = onAction,
                )
            }
        }
    }
}

private sealed interface ConfirmationDialogState {
    val draft: MemberInputUiModel
}

private data class ConfirmationDialogStateImpl(
    override val draft: MemberInputUiModel,
) : ConfirmationDialogState

private data class SubmittingDialogState(
    override val draft: MemberInputUiModel,
) : ConfirmationDialogState

private data class FailedDialogState(
    override val draft: MemberInputUiModel,
) : ConfirmationDialogState

private fun confirmationDialogState(draft: MemberInputUiModel): ConfirmationDialogState =
    ConfirmationDialogStateImpl(draft)

@Composable
private fun RegistrationAvatar(name: String) {
    Box(
        modifier =
            Modifier
                .size(AppSpacing.xxxl + AppSpacing.xxl)
                .background(InsColors.PurpleLight, AppShape.Pill),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.trim().firstOrNull()?.toString() ?: "?",
            style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = InsColors.Purple,
        )
    }
}

@Composable
private fun RegistrationConfirmationActions(
    state: ConfirmationDialogState,
    onAction: (MemberRegistrationAction) -> Unit,
) {
    val isSubmitting = state is SubmittingDialogState
    val isFailed = state is FailedDialogState
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Button(
            onClick = { onAction(MemberRegistrationAction.CancelConfirmation) },
            modifier = Modifier.weight(1f).heightIn(min = AppSpacing.xxxl + AppSpacing.lg),
            enabled = !isSubmitting,
            shape = AppShape.Card,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = InsColors.SurfaceVariant,
                    contentColor = InsColors.TextPrimary,
                    disabledContainerColor = InsColors.SurfaceVariant,
                    disabledContentColor = InsColors.TextTertiary,
                ),
        ) {
            Text(
                text = stringResource(Res.string.instructor_member_registration_confirm_cancel),
                style = appTypography().bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
        Button(
            onClick = {
                onAction(
                    when {
                        isFailed -> MemberRegistrationAction.Retry
                        else -> MemberRegistrationAction.ConfirmRegistration
                    },
                )
            },
            modifier = Modifier.weight(1f).heightIn(min = AppSpacing.xxxl + AppSpacing.lg),
            enabled = !isSubmitting,
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
                    text = stringResource(Res.string.instructor_member_registration_confirm_submitting),
                    modifier = Modifier.padding(start = AppSpacing.xs),
                    style = appTypography().bodyLarge.copy(fontWeight = FontWeight.Bold),
                )
            } else {
                Text(
                    text =
                        stringResource(
                            if (isFailed) {
                                Res.string.instructor_member_registration_confirm_retry
                            } else {
                                Res.string.instructor_member_registration_confirm_register
                            },
                        ),
                    style = appTypography().bodyLarge.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

private val memberRegistrationConfirmDraft =
    MemberInputUiModel(name = "김민지", phoneNumber = "01012345678")

@Preview(
    name = "Waiting · Instructor",
    group = "Dialog/MemberRegistration",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MemberRegistrationConfirmDialogPreview_Waiting() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        MemberRegistrationConfirmDialog(
            state = MemberRegistrationUiState.Confirmation(memberRegistrationConfirmDraft),
            onAction = {},
        )
    }
}

@Preview(
    name = "Submitting · Instructor",
    group = "Dialog/MemberRegistration",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MemberRegistrationConfirmDialogPreview_Submitting() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        MemberRegistrationConfirmDialog(
            state = MemberRegistrationUiState.Submitting(memberRegistrationConfirmDraft),
            onAction = {},
        )
    }
}

@Preview(
    name = "Failed · Instructor",
    group = "Dialog/MemberRegistration",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MemberRegistrationConfirmDialogPreview_Failed() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        MemberRegistrationConfirmDialog(
            state =
                MemberRegistrationUiState.Error(
                    draft = memberRegistrationConfirmDraft,
                    reason = MemberRegistrationUiError.NETWORK,
                ),
            onAction = {},
        )
    }
}
