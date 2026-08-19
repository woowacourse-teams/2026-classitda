package com.classitda.feature.student.myschedule.component.detail.waitlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_error
import classitda.shared.generated.resources.my_schedule_retry
import classitda.shared.generated.resources.my_schedule_waitlist_approve_confirm_description
import classitda.shared.generated.resources.my_schedule_waitlist_approve_confirm_dismiss
import classitda.shared.generated.resources.my_schedule_waitlist_approve_confirm_failed
import classitda.shared.generated.resources.my_schedule_waitlist_approve_confirm_submit
import classitda.shared.generated.resources.my_schedule_waitlist_approve_confirm_submitting
import classitda.shared.generated.resources.my_schedule_waitlist_approve_confirm_title
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.component.common.MyScheduleTextButton
import com.classitda.feature.student.myschedule.component.common.MyScheduleWarningButton
import com.classitda.feature.student.myschedule.contract.WaitlistApprovalDialogUiState
import com.classitda.feature.student.myschedule.contract.WaitlistApprovalErrorUiModel
import com.classitda.feature.student.myschedule.contract.canDismiss
import com.classitda.feature.student.myschedule.preview.WaitlistDetailPreviewFixture
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WaitlistApprovalConfirmDialog(
    position: Int,
    state: WaitlistApprovalDialogUiState,
    onConfirm: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canDismiss = state.canDismiss
    val title = stringResource(Res.string.my_schedule_waitlist_approve_confirm_title)

    Dialog(
        onDismissRequest = {
            if (canDismiss) onDismiss()
        },
        properties =
            DialogProperties(
                dismissOnBackPress = canDismiss,
                dismissOnClickOutside = canDismiss,
                usePlatformDefaultWidth = false,
            ),
    ) {
        Surface(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.xxl)
                    .semantics { paneTitle = title },
            shape = AppShape.Card,
            color = StuColors.Surface,
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionGap),
            ) {
                Box(
                    modifier =
                        Modifier
                            .background(
                                color = StuColors.Orange,
                                shape = AppShape.Pill,
                            ).padding(AppSpacing.md),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_error),
                        contentDescription = null,
                        tint = StuColors.White,
                    )
                }
                Text(
                    text = title,
                    style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = StuColors.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text =
                        stringResource(
                            Res.string.my_schedule_waitlist_approve_confirm_description,
                            position,
                        ),
                    style = appTypography().bodyMedium,
                    color = StuColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
                if (state is WaitlistApprovalDialogUiState.Failed) {
                    Text(
                        text = stringResource(Res.string.my_schedule_waitlist_approve_confirm_failed),
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        style = appTypography().bodySmall,
                        color = StuColors.Red,
                        textAlign = TextAlign.Center,
                    )
                }
                WaitlistApprovalActions(
                    state = state,
                    onConfirm = onConfirm,
                    onRetry = onRetry,
                    onDismiss = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun WaitlistApprovalActions(
    state: WaitlistApprovalDialogUiState,
    onConfirm: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isSubmitting = state is WaitlistApprovalDialogUiState.Submitting
    val isFailed = state is WaitlistApprovalDialogUiState.Failed
    val submitText =
        when {
            isSubmitting -> stringResource(Res.string.my_schedule_waitlist_approve_confirm_submitting)
            isFailed -> stringResource(Res.string.my_schedule_retry)
            else -> stringResource(Res.string.my_schedule_waitlist_approve_confirm_submit)
        }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        MyScheduleWarningButton(
            text = submitText,
            onClick = if (isFailed) onRetry else onConfirm,
            enabled = !isSubmitting,
        )
        MyScheduleTextButton(
            text = stringResource(Res.string.my_schedule_waitlist_approve_confirm_dismiss),
            onClick = onDismiss,
            enabled = state.canDismiss,
        )
    }
}

@Preview(
    name = "Waitlist approval confirm dialog · Student · Waiting",
    group = "Component/MySchedule/WaitlistApproval",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun WaitlistApprovalConfirmDialogPreview_Waiting_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        WaitlistApprovalConfirmDialog(
            position = WaitlistDetailPreviewFixture.approvalRequired.currentPosition,
            state = WaitlistApprovalDialogUiState.Waiting,
            onConfirm = {},
            onRetry = {},
            onDismiss = {},
        )
    }
}

@Preview(
    name = "Waitlist approval confirm dialog · Student · Submitting",
    group = "Component/MySchedule/WaitlistApproval",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun WaitlistApprovalConfirmDialogPreview_Submitting_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        WaitlistApprovalConfirmDialog(
            position = WaitlistDetailPreviewFixture.approvalRequired.currentPosition,
            state = WaitlistApprovalDialogUiState.Submitting,
            onConfirm = {},
            onRetry = {},
            onDismiss = {},
        )
    }
}

@Preview(
    name = "Waitlist approval confirm dialog · Student · Failed",
    group = "Component/MySchedule/WaitlistApproval",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun WaitlistApprovalConfirmDialogPreview_Failed_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        WaitlistApprovalConfirmDialog(
            position = WaitlistDetailPreviewFixture.approvalRequired.currentPosition,
            state = WaitlistApprovalDialogUiState.Failed(WaitlistApprovalErrorUiModel.NETWORK),
            onConfirm = {},
            onRetry = {},
            onDismiss = {},
        )
    }
}
