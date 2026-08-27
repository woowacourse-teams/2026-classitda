package com.classitda.feature.student.myschedule.component.detail.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_error
import classitda.shared.generated.resources.ic_info
import classitda.shared.generated.resources.my_schedule_cancel_confirm_availability_label
import classitda.shared.generated.resources.my_schedule_cancel_confirm_available_count
import classitda.shared.generated.resources.my_schedule_cancel_confirm_dismiss
import classitda.shared.generated.resources.my_schedule_cancel_confirm_failed
import classitda.shared.generated.resources.my_schedule_cancel_confirm_irreversible
import classitda.shared.generated.resources.my_schedule_cancel_confirm_policy_emphasis
import classitda.shared.generated.resources.my_schedule_cancel_confirm_policy_prefix
import classitda.shared.generated.resources.my_schedule_cancel_confirm_policy_suffix
import classitda.shared.generated.resources.my_schedule_cancel_confirm_submit
import classitda.shared.generated.resources.my_schedule_cancel_confirm_submitting
import classitda.shared.generated.resources.my_schedule_cancel_confirm_title
import classitda.shared.generated.resources.my_schedule_retry
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.component.common.MyScheduleDestructiveButton
import com.classitda.feature.student.myschedule.component.common.MyScheduleTextButton
import com.classitda.feature.student.myschedule.contract.ReservationCancellationAvailabilityUiModel
import com.classitda.feature.student.myschedule.contract.ReservationCancellationDialogUiState
import com.classitda.feature.student.myschedule.contract.ReservationCancellationErrorUiModel
import com.classitda.feature.student.myschedule.contract.ReservationDetailUiModel
import com.classitda.feature.student.myschedule.contract.canDismiss
import com.classitda.feature.student.myschedule.preview.ReservationDetailPreviewFixture
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReservationCancellationConfirmDialog(
    reservation: ReservationDetailUiModel.Confirmed,
    state: ReservationCancellationDialogUiState,
    onConfirm: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cancellation =
        reservation.cancellation as? ReservationCancellationAvailabilityUiModel.Available
            ?: return
    val canDismiss = state.canDismiss
    val title = stringResource(Res.string.my_schedule_cancel_confirm_title)

    Dialog(
        onDismissRequest = {
            if (canDismiss) {
                onDismiss()
            }
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
                    .padding(horizontal = AppSpacing.xxxl)
                    .semantics { paneTitle = title },
            shape = AppShape.Card,
            color = StuColors.Surface,
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                ReservationCancellationWarningIcon()
                Text(
                    text = title,
                    style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = StuColors.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                ReservationCancellationPolicy(
                    cancellableUses = reservation.pass.cancellableUses,
                    hoursUntilStart = cancellation.hoursUntilStart,
                    restoredPassUses = cancellation.restoredPassUses,
                )
                Text(
                    text = stringResource(Res.string.my_schedule_cancel_confirm_irreversible),
                    style = appTypography().bodySmall,
                    color = StuColors.TextTertiary,
                    textAlign = TextAlign.Center,
                )
                if (state is ReservationCancellationDialogUiState.Failed) {
                    Text(
                        text = stringResource(Res.string.my_schedule_cancel_confirm_failed),
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        style = appTypography().bodySmall,
                        color = StuColors.Red,
                        textAlign = TextAlign.Center,
                    )
                }
                ReservationCancellationActions(
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
private fun ReservationCancellationWarningIcon() {
    Box(
        modifier =
            Modifier
                .background(
                    color = StuColors.Red,
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
}

@Composable
private fun ReservationCancellationPolicy(
    cancellableUses: Int,
    hoursUntilStart: Int,
    restoredPassUses: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShape.Card,
        color = StuColors.SurfaceVariant,
    ) {
        Column(modifier = Modifier.padding(AppSpacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.my_schedule_cancel_confirm_availability_label),
                    style = appTypography().bodyMedium,
                    color = StuColors.TextTertiary,
                )
                Text(
                    text =
                        stringResource(
                            Res.string.my_schedule_cancel_confirm_available_count,
                            cancellableUses,
                        ),
                    style = appTypography().bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = StuColors.TextPrimary,
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = AppSpacing.md),
                color = StuColors.Divider,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_info),
                    contentDescription = null,
                    tint = StuColors.TextTertiary,
                )
                Text(
                    text =
                        buildAnnotatedString {
                            append(
                                stringResource(
                                    Res.string.my_schedule_cancel_confirm_policy_prefix,
                                    hoursUntilStart,
                                ),
                            )
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(
                                    stringResource(
                                        Res.string.my_schedule_cancel_confirm_policy_emphasis,
                                        restoredPassUses,
                                    ),
                                )
                            }
                            append(stringResource(Res.string.my_schedule_cancel_confirm_policy_suffix))
                        },
                    style = appTypography().bodySmall,
                    color = StuColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun ReservationCancellationActions(
    state: ReservationCancellationDialogUiState,
    onConfirm: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isSubmitting = state is ReservationCancellationDialogUiState.Submitting
    val isFailed = state is ReservationCancellationDialogUiState.Failed
    val submitText =
        when {
            isSubmitting -> stringResource(Res.string.my_schedule_cancel_confirm_submitting)
            isFailed -> stringResource(Res.string.my_schedule_retry)
            else -> stringResource(Res.string.my_schedule_cancel_confirm_submit)
        }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        MyScheduleDestructiveButton(
            text = submitText,
            onClick = if (isFailed) onRetry else onConfirm,
            enabled = !isSubmitting,
        )
        MyScheduleTextButton(
            text = stringResource(Res.string.my_schedule_cancel_confirm_dismiss),
            onClick = onDismiss,
            enabled = state.canDismiss,
        )
    }
}

@Preview(
    name = "Reservation cancellation dialog / Waiting / Student",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
    heightDp = 760,
)
@Composable
private fun ReservationCancellationConfirmDialogPreview_Waiting_Student() {
    ReservationCancellationConfirmDialogPreview(
        state = ReservationCancellationDialogUiState.Waiting,
    )
}

@Preview(
    name = "Reservation cancellation dialog / Submitting / Student",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
    heightDp = 760,
)
@Composable
private fun ReservationCancellationConfirmDialogPreview_Submitting_Student() {
    ReservationCancellationConfirmDialogPreview(
        state = ReservationCancellationDialogUiState.Submitting,
    )
}

@Preview(
    name = "Reservation cancellation dialog / Failed / Student",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
    heightDp = 760,
)
@Composable
private fun ReservationCancellationConfirmDialogPreview_Failed_Student() {
    ReservationCancellationConfirmDialogPreview(
        state =
            ReservationCancellationDialogUiState.Failed(
                error = ReservationCancellationErrorUiModel.NETWORK,
            ),
    )
}

@Composable
private fun ReservationCancellationConfirmDialogPreview(state: ReservationCancellationDialogUiState) {
    AppTheme(theme = ThemeType.STUDENT) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(StuColors.Background),
        ) {
            ReservationCancellationConfirmDialog(
                reservation = ReservationDetailPreviewFixture.confirmed,
                state = state,
                onConfirm = {},
                onRetry = {},
                onDismiss = {},
            )
        }
    }
}
