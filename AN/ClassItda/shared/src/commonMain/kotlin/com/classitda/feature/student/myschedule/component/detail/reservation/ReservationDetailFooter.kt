package com.classitda.feature.student.myschedule.component.detail.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_bullet
import classitda.shared.generated.resources.my_schedule_cancel_reservation
import classitda.shared.generated.resources.my_schedule_class_detail_absent_result
import classitda.shared.generated.resources.my_schedule_class_detail_attended_result
import classitda.shared.generated.resources.my_schedule_class_detail_center_cancellation_policy
import classitda.shared.generated.resources.my_schedule_class_detail_history_condition
import classitda.shared.generated.resources.my_schedule_class_detail_notice
import classitda.shared.generated.resources.my_schedule_class_detail_reservation_cancellation_deadline
import classitda.shared.generated.resources.my_schedule_class_detail_ticket_inquiry
import classitda.shared.generated.resources.my_schedule_class_detail_waitlist_confirmation_policy
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.component.common.MyScheduleDestructiveButton
import com.classitda.feature.student.myschedule.contract.ReservationDetailUiModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReservationDetailFooter(
    model: ReservationDetailUiModel,
    onCancelReservation: (() -> Unit)?,
) {
    when (model) {
        is ReservationDetailUiModel.Confirmed -> {
            ReservationDetailNotice(
                notices =
                    listOf(
                        stringResource(
                            Res.string.my_schedule_class_detail_reservation_cancellation_deadline,
                            model.cancellationDeadlineHoursBeforeStart,
                        ),
                        stringResource(Res.string.my_schedule_class_detail_waitlist_confirmation_policy),
                    ),
            )
            ReservationCancellationAction(onCancelReservation = onCancelReservation)
        }

        is ReservationDetailUiModel.Cancelled -> {}

        is ReservationDetailUiModel.Attended -> {
            ReservationDetailNotice(
                notices =
                    listOf(
                        stringResource(Res.string.my_schedule_class_detail_ticket_inquiry),
                        stringResource(Res.string.my_schedule_class_detail_center_cancellation_policy),
                    ),
            )
            ReservationResultBanner(
                text = stringResource(Res.string.my_schedule_class_detail_attended_result),
            )
        }

        is ReservationDetailUiModel.Absent -> {
            ReservationDetailNotice(
                notices =
                    listOf(
                        stringResource(Res.string.my_schedule_class_detail_history_condition),
                        stringResource(Res.string.my_schedule_class_detail_ticket_inquiry),
                        stringResource(Res.string.my_schedule_class_detail_center_cancellation_policy),
                    ),
            )
            ReservationResultBanner(
                text = stringResource(Res.string.my_schedule_class_detail_absent_result),
            )
        }
    }
}

@Composable
private fun ReservationDetailNotice(notices: List<String>) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppSpacing.screenPadding,
                    vertical = AppSpacing.xxl,
                ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            text = stringResource(Res.string.my_schedule_class_detail_notice),
            style = appTypography().titleSmall.copy(fontWeight = FontWeight.Medium),
            color = StuColors.TextSecondary,
        )
        notices.forEach { notice ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = stringResource(Res.string.my_schedule_bullet),
                    style = appTypography().bodySmall,
                    color = StuColors.TextTertiary,
                )
                Text(
                    text = notice,
                    modifier = Modifier.weight(1f),
                    style = appTypography().bodySmall,
                    color = StuColors.TextTertiary,
                )
            }
        }
    }
}

@Composable
private fun ReservationCancellationAction(onCancelReservation: (() -> Unit)?) {
    MyScheduleDestructiveButton(
        text = stringResource(Res.string.my_schedule_cancel_reservation),
        onClick = { onCancelReservation?.invoke() },
        modifier =
            Modifier.padding(
                horizontal = AppSpacing.screenPadding,
                vertical = AppSpacing.xxl,
            ),
        enabled = onCancelReservation != null,
    )
}

@Composable
private fun ReservationResultBanner(text: String) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppSpacing.screenPadding,
                    vertical = AppSpacing.xxl,
                ).background(
                    color = StuColors.SurfaceVariant,
                    shape = AppShape.Card,
                ).padding(AppSpacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = appTypography().bodyMedium,
            color = StuColors.TextSecondary,
        )
    }
}
