package com.classitda.feature.student.myschedule.component.detail.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import classitda.shared.generated.resources.my_schedule_cancel_confirm_available
import classitda.shared.generated.resources.my_schedule_cancel_confirm_dismiss
import classitda.shared.generated.resources.my_schedule_cancel_confirm_irreversible
import classitda.shared.generated.resources.my_schedule_cancel_confirm_policy_emphasis
import classitda.shared.generated.resources.my_schedule_cancel_confirm_policy_prefix
import classitda.shared.generated.resources.my_schedule_cancel_confirm_policy_suffix
import classitda.shared.generated.resources.my_schedule_cancel_confirm_title
import classitda.shared.generated.resources.my_schedule_cancel_reservation
import com.classitda.core.designsystem.AppColor
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.component.common.MyScheduleDestructiveButton
import com.classitda.feature.student.myschedule.component.common.MyScheduleTextButton
import com.classitda.feature.student.myschedule.preview.reservationDetailPreviewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReservationCancellationConfirmDialog(
    hoursUntilStart: Int,
    restoredTicketCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(Res.string.my_schedule_cancel_confirm_title)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
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
                modifier = Modifier.padding(AppSpacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionGap),
            ) {
                Box(
                    modifier =
                        Modifier
                            .background(
                                color = AppColor.SecondaryRed,
                                shape = AppShape.Pill,
                            ).padding(AppSpacing.md),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_error),
                        contentDescription = null,
                        tint = AppColor.AccentRed,
                    )
                }
                Text(
                    text = title,
                    style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = StuColors.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                ReservationCancellationAvailability(
                    hoursUntilStart = hoursUntilStart,
                    restoredTicketCount = restoredTicketCount,
                )
                Text(
                    text = stringResource(Res.string.my_schedule_cancel_confirm_irreversible),
                    style = appTypography().bodySmall,
                    color = StuColors.TextTertiary,
                    textAlign = TextAlign.Center,
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    MyScheduleDestructiveButton(
                        text = stringResource(Res.string.my_schedule_cancel_reservation),
                        onClick = onConfirm,
                    )
                    MyScheduleTextButton(
                        text = stringResource(Res.string.my_schedule_cancel_confirm_dismiss),
                        onClick = onDismiss,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReservationCancellationAvailability(
    hoursUntilStart: Int,
    restoredTicketCount: Int,
) {
    val typography = appTypography()
    val policyPrefix =
        stringResource(
            Res.string.my_schedule_cancel_confirm_policy_prefix,
            hoursUntilStart,
        )
    val policyEmphasis =
        stringResource(
            Res.string.my_schedule_cancel_confirm_policy_emphasis,
            restoredTicketCount,
        )
    val policySuffix = stringResource(Res.string.my_schedule_cancel_confirm_policy_suffix)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    color = StuColors.SurfaceVariant,
                    shape = AppShape.Card,
                ).padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.my_schedule_cancel_confirm_availability_label),
                style = typography.bodyLarge,
                color = StuColors.TextSecondary,
            )
            Text(
                text = stringResource(Res.string.my_schedule_cancel_confirm_available),
                style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = StuColors.PrimaryGreen,
            )
        }
        HorizontalDivider(color = StuColors.Divider)
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_info),
                contentDescription = null,
                tint = StuColors.PrimaryGreen,
            )
            Text(
                text =
                    buildAnnotatedString {
                        append(policyPrefix)
                        withStyle(
                            SpanStyle(
                                color = StuColors.PrimaryGreen,
                                fontWeight = FontWeight.Bold,
                            ),
                        ) {
                            append(policyEmphasis)
                        }
                        append(policySuffix)
                    },
                style = typography.bodyMedium,
                color = StuColors.TextSecondary,
            )
        }
    }
}

@Preview(
    name = "Reservation cancellation confirm dialog · Student · Default",
    group = "Component/MySchedule",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ReservationCancellationConfirmDialogPreview_Confirming_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(StuColors.Background),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ReservationDetailTopBar(onBack = {})
                ReservationDetailContent(
                    model = reservationDetailPreviewModel(),
                    onInquiry = {},
                    onCancelReservation = {},
                    modifier = Modifier.weight(1f),
                )
            }
            ReservationCancellationConfirmDialog(
                hoursUntilStart = 22,
                restoredTicketCount = 1,
                onConfirm = {},
                onDismiss = {},
            )
        }
    }
}
