package com.classitda.feature.student.myschedule.component.result.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_error
import classitda.shared.generated.resources.my_schedule_cancellation_history
import classitda.shared.generated.resources.my_schedule_cancelled_at
import classitda.shared.generated.resources.my_schedule_cancelled_at_label
import classitda.shared.generated.resources.my_schedule_class_information
import classitda.shared.generated.resources.my_schedule_class_name
import classitda.shared.generated.resources.my_schedule_date_time
import classitda.shared.generated.resources.my_schedule_instructor
import classitda.shared.generated.resources.my_schedule_instructor_name
import classitda.shared.generated.resources.my_schedule_location
import classitda.shared.generated.resources.my_schedule_refund_restoration_status
import classitda.shared.generated.resources.my_schedule_reservation_cancelled_description
import classitda.shared.generated.resources.my_schedule_reservation_cancelled_title
import classitda.shared.generated.resources.my_schedule_ticket_restoration_completed
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.component.result.common.MyScheduleResultInformationRow
import com.classitda.feature.student.myschedule.component.result.common.MyScheduleResultSectionTitle
import com.classitda.feature.student.myschedule.contract.ReservationDetailUiModel
import com.classitda.feature.student.myschedule.preview.reservationDetailPreviewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReservationCancelledContent(
    reservation: ReservationDetailUiModel,
    cancelledAtLabel: String,
    restoredTicketCount: Int,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
        item {
            ReservationCancelledHero()
        }
        item {
            ReservationCancelledClassInformation(reservation = reservation)
        }
        item {
            ReservationCancellationHistory(
                cancelledAtLabel = cancelledAtLabel,
                restoredTicketCount = restoredTicketCount,
            )
        }
    }
}

@Composable
private fun ReservationCancelledHero() {
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_error),
            contentDescription = null,
            tint = StuColors.PrimaryGreen,
        )
        Text(
            text = stringResource(Res.string.my_schedule_reservation_cancelled_title),
            modifier = Modifier.semantics { heading() },
            style = typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.my_schedule_reservation_cancelled_description),
            style = typography.bodyMedium,
            color = StuColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ReservationCancelledClassInformation(reservation: ReservationDetailUiModel) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.Surface)
                .padding(AppSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        MyScheduleResultSectionTitle(
            text = stringResource(Res.string.my_schedule_class_information),
        )
        MyScheduleResultInformationRow(
            label = stringResource(Res.string.my_schedule_class_name),
            value = reservation.title,
        )
        MyScheduleResultInformationRow(
            label = stringResource(Res.string.my_schedule_instructor),
            value = stringResource(Res.string.my_schedule_instructor_name, reservation.instructorName),
        )
        MyScheduleResultInformationRow(
            label = stringResource(Res.string.my_schedule_date_time),
            value = reservation.dateTime.dateLabel,
            supportingValue = reservation.dateTime.timeRangeLabel,
        )
        MyScheduleResultInformationRow(
            label = stringResource(Res.string.my_schedule_location),
            value = reservation.locationLabel,
        )
    }
}

@Composable
private fun ReservationCancellationHistory(
    cancelledAtLabel: String,
    restoredTicketCount: Int,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.Surface)
                .padding(AppSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        MyScheduleResultSectionTitle(
            text = stringResource(Res.string.my_schedule_cancellation_history),
        )
        MyScheduleResultInformationRow(
            label = stringResource(Res.string.my_schedule_cancelled_at),
            value = cancelledAtLabel,
        )
        MyScheduleResultInformationRow(
            label = stringResource(Res.string.my_schedule_refund_restoration_status),
            value =
                stringResource(
                    Res.string.my_schedule_ticket_restoration_completed,
                    restoredTicketCount,
                ),
            valueColor = StuColors.PrimaryGreen,
        )
    }
}

@Preview(
    name = "Reservation cancelled content · Student · Default",
    group = "Component/MySchedule",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 660,
)
@Composable
private fun ReservationCancelledContentPreview_Success_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationCancelledContent(
            reservation = reservationDetailPreviewModel().copy(locationLabel = "리포머룸"),
            cancelledAtLabel =
                stringResource(
                    Res.string.my_schedule_cancelled_at_label,
                    2026,
                    "08",
                    "04",
                    "14:32",
                ),
            restoredTicketCount = 1,
        )
    }
}
