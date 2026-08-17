package com.classitda.feature.student.myschedule.component.detail.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_schedule
import classitda.shared.generated.resources.my_schedule_attendance_time
import classitda.shared.generated.resources.my_schedule_cancelled_at
import classitda.shared.generated.resources.my_schedule_reserved_at
import classitda.shared.generated.resources.my_schedule_status_absent
import classitda.shared.generated.resources.my_schedule_status_attended
import classitda.shared.generated.resources.my_schedule_status_completed_mark
import classitda.shared.generated.resources.my_schedule_status_confirmed
import classitda.shared.generated.resources.my_schedule_status_confirmed_mark
import classitda.shared.generated.resources.my_schedule_status_reservation_canceled
import classitda.shared.generated.resources.my_schedule_status_reservation_canceled_mark
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.contract.ReservationDetailUiModel
import com.classitda.feature.student.myschedule.preview.ReservationDetailPreviewFixture
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReservationDetailSummary(model: ReservationDetailUiModel) {
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
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xxl),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
            ReservationDetailStatusLabel(model = model)
            Text(
                text = model.title,
                modifier = Modifier.semantics { heading() },
                style = typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = StuColors.TextPrimary,
            )
        }
        ReservationDetailInformationRow(
            icon = Res.drawable.ic_schedule,
            label = stringResource(model.primaryTimeLabel()),
        ) {
            Text(
                text = model.primaryTimeValue(),
                style = typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = StuColors.TextPrimary,
            )
        }
    }
}

@Composable
private fun ReservationDetailStatusLabel(model: ReservationDetailUiModel) {
    val presentation = model.statusPresentation()

    Row(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(presentation.mark),
            style = appTypography().titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = presentation.color,
        )
        Text(
            text = stringResource(presentation.label),
            style = appTypography().titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = presentation.color,
        )
    }
}

private data class ReservationDetailStatusPresentation(
    val label: StringResource,
    val mark: StringResource,
    val color: Color,
)

private fun ReservationDetailUiModel.statusPresentation(): ReservationDetailStatusPresentation =
    when (this) {
        is ReservationDetailUiModel.Confirmed -> {
            ReservationDetailStatusPresentation(
                label = Res.string.my_schedule_status_confirmed,
                mark = Res.string.my_schedule_status_confirmed_mark,
                color = StuColors.Green,
            )
        }

        is ReservationDetailUiModel.Cancelled -> {
            ReservationDetailStatusPresentation(
                label = Res.string.my_schedule_status_reservation_canceled,
                mark = Res.string.my_schedule_status_reservation_canceled_mark,
                color = StuColors.Red,
            )
        }

        is ReservationDetailUiModel.Attended -> {
            ReservationDetailStatusPresentation(
                label = Res.string.my_schedule_status_attended,
                mark = Res.string.my_schedule_status_completed_mark,
                color = StuColors.TextSecondary,
            )
        }

        is ReservationDetailUiModel.Absent -> {
            ReservationDetailStatusPresentation(
                label = Res.string.my_schedule_status_absent,
                mark = Res.string.my_schedule_status_reservation_canceled_mark,
                color = StuColors.TextSecondary,
            )
        }
    }

private fun ReservationDetailUiModel.primaryTimeLabel(): StringResource =
    when (this) {
        is ReservationDetailUiModel.Confirmed -> Res.string.my_schedule_reserved_at
        is ReservationDetailUiModel.Cancelled -> Res.string.my_schedule_cancelled_at
        is ReservationDetailUiModel.Attended -> Res.string.my_schedule_attendance_time
        is ReservationDetailUiModel.Absent -> Res.string.my_schedule_attendance_time
    }

private fun ReservationDetailUiModel.primaryTimeValue(): String =
    when (this) {
        is ReservationDetailUiModel.Confirmed -> reservedAtLabel
        is ReservationDetailUiModel.Cancelled -> cancelledAtLabel
        is ReservationDetailUiModel.Attended -> checkedInAtLabel
        is ReservationDetailUiModel.Absent -> attendanceTimePlaceholder
    }

@Preview(
    name = "Reservation detail summary / Confirmed / Student",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun ReservationDetailSummaryPreview_Confirmed_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationDetailSummary(model = ReservationDetailPreviewFixture.confirmed)
    }
}
