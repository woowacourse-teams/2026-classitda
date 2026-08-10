package com.classitda.feature.student.myschedule.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_chat_bubble_outline
import classitda.shared.generated.resources.ic_location_on
import classitda.shared.generated.resources.ic_schedule
import classitda.shared.generated.resources.my_schedule_bullet
import classitda.shared.generated.resources.my_schedule_cancel_reservation
import classitda.shared.generated.resources.my_schedule_detail_date_time_label
import classitda.shared.generated.resources.my_schedule_inquiry
import classitda.shared.generated.resources.my_schedule_inquiry_icon
import classitda.shared.generated.resources.my_schedule_instructor_name
import classitda.shared.generated.resources.my_schedule_remaining_count
import classitda.shared.generated.resources.my_schedule_reservation_attendee_count
import classitda.shared.generated.resources.my_schedule_reservation_attendee_count_value
import classitda.shared.generated.resources.my_schedule_reservation_available
import classitda.shared.generated.resources.my_schedule_reservation_cancellation_deadline
import classitda.shared.generated.resources.my_schedule_reservation_cancellation_guide
import classitda.shared.generated.resources.my_schedule_reservation_confirmed
import classitda.shared.generated.resources.my_schedule_reservation_information
import classitda.shared.generated.resources.my_schedule_reservation_status
import classitda.shared.generated.resources.my_schedule_reservation_ticket
import classitda.shared.generated.resources.my_schedule_reservation_ticket_no_deduction
import classitda.shared.generated.resources.my_schedule_reservation_ticket_restoration_facility
import classitda.shared.generated.resources.my_schedule_schedule_location
import classitda.shared.generated.resources.my_schedule_schedule_time
import classitda.shared.generated.resources.my_schedule_separator
import classitda.shared.generated.resources.my_schedule_used_ticket
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.contract.ReservationDetailUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleCancellationAvailabilityUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleCancellationPolicyUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleTicketRestorationUiModel
import com.classitda.feature.student.myschedule.preview.reservationDetailPreviewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReservationDetailContent(
    model: ReservationDetailUiModel,
    onInquiry: () -> Unit,
    onCancelReservation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background),
    ) {
        item {
            ReservationSummarySection(
                model = model,
                onInquiry = onInquiry,
            )
        }
        item {
            ReservationTicketSection(model = model)
        }
        item {
            ReservationInformationSection(model = model)
        }
        item {
            ReservationCancellationSection(
                model = model,
                onCancelReservation = onCancelReservation,
            )
        }
    }
}

@Composable
private fun ReservationSummarySection(
    model: ReservationDetailUiModel,
    onInquiry: () -> Unit,
) {
    val typography = appTypography()

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.Surface)
                .padding(horizontal = AppSpacing.screenPadding)
                .padding(top = AppSpacing.xxl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        ScheduleStatusChip(
            type = ScheduleStatusChipType.CONFIRMED_RESERVATION,
            labelOverride = Res.string.my_schedule_reservation_confirmed,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = model.title,
                modifier = Modifier.weight(1f),
                style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = StuColors.TextPrimary,
            )
            Text(
                text = stringResource(Res.string.my_schedule_instructor_name, model.instructorName),
                style = typography.bodyMedium,
                color = StuColors.TextSecondary,
            )
        }
        ReservationDetailIconRow(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_schedule),
                    contentDescription = stringResource(Res.string.my_schedule_schedule_time),
                    modifier = Modifier.size(AppSpacing.xl),
                    tint = StuColors.TextSecondary,
                )
            },
            text =
                stringResource(
                    Res.string.my_schedule_detail_date_time_label,
                    model.dateTime.dateLabel,
                    model.dateTime.timeRangeLabel,
                ),
        )
        ReservationDetailIconRow(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_location_on),
                    contentDescription = stringResource(Res.string.my_schedule_schedule_location),
                    modifier = Modifier.size(AppSpacing.xl),
                    tint = StuColors.TextPrimary,
                )
            },
            text = model.locationLabel,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier =
                Modifier
                    .align(Alignment.End)
                    .clickable(
                        role = Role.Button,
                        onClick = onInquiry,
                    ).padding(
                        horizontal = AppSpacing.sm,
                        vertical = AppSpacing.xs,
                    ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_chat_bubble_outline),
                contentDescription = stringResource(Res.string.my_schedule_inquiry_icon),
                modifier = Modifier.size(AppSpacing.xl),
                tint = StuColors.TextSecondary,
            )
            Text(
                text = stringResource(Res.string.my_schedule_inquiry),
                modifier = Modifier.padding(start = AppSpacing.sm),
                style = typography.bodyMedium,
                color = StuColors.TextSecondary,
            )
        }
        HorizontalDivider(color = StuColors.Divider)
    }
}

@Composable
private fun ReservationDetailIconRow(
    icon: @Composable () -> Unit,
    text: String,
    fontWeight: FontWeight = FontWeight.Normal,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Text(
            text = text,
            style = appTypography().bodyMedium.copy(fontWeight = fontWeight),
            color = StuColors.TextPrimary,
        )
    }
}

@Composable
private fun ReservationTicketSection(model: ReservationDetailUiModel) {
    val typography = appTypography()

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.Surface)
                .padding(
                    horizontal = AppSpacing.screenPadding,
                    vertical = AppSpacing.xxl,
                ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            text = stringResource(Res.string.my_schedule_used_ticket),
            style = typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = StuColors.TextSecondary,
        )
        Text(
            text = model.ticket.name,
            style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = model.ticket.validUntilLabel,
                style = typography.bodyMedium,
                color = StuColors.TextSecondary,
            )
            Text(
                text = stringResource(Res.string.my_schedule_separator),
                style = typography.bodyMedium,
                color = StuColors.Divider,
            )
            Text(
                text = stringResource(Res.string.my_schedule_reservation_available),
                style = typography.bodyMedium,
                color = StuColors.TextSecondary,
            )
            Text(
                text =
                    stringResource(
                        Res.string.my_schedule_remaining_count,
                        model.ticket.remainingReservationCount,
                    ),
                style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = StuColors.PrimaryGreen,
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = AppSpacing.xxl),
            color = StuColors.Divider,
        )
    }
}

@Composable
private fun ReservationInformationSection(model: ReservationDetailUiModel) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.Surface)
                .padding(
                    horizontal = AppSpacing.screenPadding,
                    vertical = AppSpacing.xxl,
                ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        Text(
            text = stringResource(Res.string.my_schedule_reservation_information),
            style = appTypography().titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = StuColors.TextSecondary,
        )
        ReservationInformationRow(
            label = stringResource(Res.string.my_schedule_reservation_status),
            value = stringResource(Res.string.my_schedule_reservation_confirmed),
        )
        ReservationInformationRow(
            label = stringResource(Res.string.my_schedule_reservation_attendee_count),
            value =
                stringResource(
                    Res.string.my_schedule_reservation_attendee_count_value,
                    model.attendeeCount,
                ),
        )
        ReservationInformationRow(
            label = stringResource(Res.string.my_schedule_reservation_ticket),
            value = model.ticket.name,
        )
    }
}

@Composable
private fun ReservationInformationRow(
    label: String,
    value: String,
) {
    val typography = appTypography()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = typography.bodyLarge,
            color = StuColors.TextSecondary,
        )
        Text(
            text = value,
            style = typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = StuColors.TextPrimary,
        )
    }
}

@Composable
private fun ReservationCancellationSection(
    model: ReservationDetailUiModel,
    onCancelReservation: () -> Unit,
) {
    val policy = model.cancellation.policy as ScheduleCancellationPolicyUiModel.Reservation

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.Background)
                .padding(horizontal = AppSpacing.screenPadding)
                .padding(
                    top = AppSpacing.xxxl,
                    bottom = AppSpacing.screenPadding,
                ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        Text(
            text = stringResource(Res.string.my_schedule_reservation_cancellation_guide),
            style = appTypography().titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = StuColors.TextSecondary,
        )
        ReservationCancellationBullet(
            text =
                stringResource(
                    Res.string.my_schedule_reservation_cancellation_deadline,
                    policy.deadlineHoursBeforeStart,
                ),
        )
        ReservationCancellationBullet(
            text =
                stringResource(
                    when (policy.ticketRestoration) {
                        ScheduleTicketRestorationUiModel.AccordingToFacilityPolicy -> {
                            Res.string.my_schedule_reservation_ticket_restoration_facility
                        }

                        ScheduleTicketRestorationUiModel.NoDeduction -> {
                            Res.string.my_schedule_reservation_ticket_no_deduction
                        }
                    },
                ),
        )
        MyScheduleDestructiveButton(
            text = stringResource(Res.string.my_schedule_cancel_reservation),
            onClick = onCancelReservation,
            modifier = Modifier.padding(top = AppSpacing.xxxl),
            enabled = model.cancellation is ScheduleCancellationAvailabilityUiModel.Available,
        )
    }
}

@Composable
private fun ReservationCancellationBullet(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = stringResource(Res.string.my_schedule_bullet),
            style = appTypography().bodyMedium,
            color = StuColors.TextSecondary,
        )
        Text(
            text = text,
            style = appTypography().bodyMedium,
            color = StuColors.TextSecondary,
        )
    }
}

@Preview(
    name = "Reservation detail content / Student / Default",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
    heightDp = 788,
)
@Composable
private fun ReservationDetailContentPreview_Default_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationDetailContent(
            model = reservationDetailPreviewModel(),
            onInquiry = {},
            onCancelReservation = {},
        )
    }
}
