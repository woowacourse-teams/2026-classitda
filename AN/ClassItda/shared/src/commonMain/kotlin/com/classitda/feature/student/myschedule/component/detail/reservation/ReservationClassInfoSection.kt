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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_calendar_today
import classitda.shared.generated.resources.ic_chat_bubble_outline
import classitda.shared.generated.resources.ic_confirmation_number
import classitda.shared.generated.resources.my_schedule_class_detail_date
import classitda.shared.generated.resources.my_schedule_memo
import classitda.shared.generated.resources.my_schedule_pass_cancellable
import classitda.shared.generated.resources.my_schedule_pass_reservable
import classitda.shared.generated.resources.my_schedule_pass_total_remaining
import classitda.shared.generated.resources.my_schedule_separator
import classitda.shared.generated.resources.my_schedule_used_ticket
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.contract.ReservationDetailUiModel
import com.classitda.feature.student.myschedule.contract.ReservationPassAvailabilityUiModel
import com.classitda.feature.student.myschedule.contract.ReservationUsedPassUiModel
import com.classitda.feature.student.myschedule.preview.ReservationDetailPreviewFixture
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReservationClassInfoSection(model: ReservationDetailUiModel) {
    val classInfo = model.classInfo

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.Surface)
                .padding(AppSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xxl),
    ) {
        ReservationDetailInformationRow(
            icon = Res.drawable.ic_calendar_today,
            label = stringResource(Res.string.my_schedule_class_detail_date),
        ) {
            Text(
                text = classInfo.dateLabel,
                style = appTypography().bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = StuColors.TextPrimary,
            )
            Text(
                text = classInfo.timeRangeLabel,
                style = appTypography().bodyMedium,
                color = StuColors.TextSecondary,
            )
        }

        when (model) {
            is ReservationDetailUiModel.Confirmed -> ReservationPassInformation(pass = model.pass)
            is ReservationDetailUiModel.Attended -> ReservationUsedPassInformation(pass = model.usedPass)
            is ReservationDetailUiModel.Absent -> ReservationUsedPassInformation(pass = model.usedPass)
            is ReservationDetailUiModel.Cancelled -> Unit
            is ReservationDetailUiModel.ClassCancelled -> Unit
        }

        classInfo.memo?.let { memo ->
            ReservationDetailInformationRow(
                icon = Res.drawable.ic_chat_bubble_outline,
                label = stringResource(Res.string.my_schedule_memo),
            ) {
                Text(
                    text = memo,
                    style = appTypography().bodyMedium,
                    color = StuColors.TextPrimary,
                )
            }
        }
    }
}

@Composable
private fun ReservationPassInformation(pass: ReservationPassAvailabilityUiModel) {
    ReservationDetailInformationRow(
        icon = Res.drawable.ic_confirmation_number,
        label = stringResource(Res.string.my_schedule_used_ticket),
    ) {
        Text(
            text = pass.name,
            style = appTypography().bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = StuColors.TextPrimary,
        )
        Text(
            text = pass.validityLabel,
            style = appTypography().bodyMedium,
            color = StuColors.TextSecondary,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReservationPassCount(
                text = stringResource(Res.string.my_schedule_pass_total_remaining, pass.remainingUses),
            )
            ReservationPassCountSeparator()
            ReservationPassCount(
                text = stringResource(Res.string.my_schedule_pass_reservable, pass.reservableUses),
            )
            ReservationPassCountSeparator()
            ReservationPassCount(
                text = stringResource(Res.string.my_schedule_pass_cancellable, pass.cancellableUses),
                color = StuColors.Red,
            )
        }
    }
}

@Composable
private fun ReservationUsedPassInformation(pass: ReservationUsedPassUiModel) {
    ReservationDetailInformationRow(
        icon = Res.drawable.ic_confirmation_number,
        label = stringResource(Res.string.my_schedule_used_ticket),
    ) {
        Text(
            text = pass.name,
            style = appTypography().bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = StuColors.TextPrimary,
        )
        Text(
            text = pass.validityLabel,
            style = appTypography().bodyMedium,
            color = StuColors.TextSecondary,
        )
    }
}

@Composable
private fun ReservationPassCount(
    text: String,
    color: Color = StuColors.TextSecondary,
) {
    Text(
        text = text,
        style = appTypography().bodySmall,
        color = color,
    )
}

@Composable
private fun ReservationPassCountSeparator() {
    Text(
        text = stringResource(Res.string.my_schedule_separator),
        style = appTypography().bodySmall,
        color = StuColors.DividerStrong,
    )
}

@Preview(
    name = "Reservation class info / Confirmed / Student",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun ReservationClassInfoSectionPreview_Confirmed_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationClassInfoSection(model = ReservationDetailPreviewFixture.confirmed)
    }
}

@Preview(
    name = "Reservation class info / F09 attended / Student",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun ReservationClassInfoSectionPreview_F09Attended_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationClassInfoSection(model = ReservationDetailPreviewFixture.attended)
    }
}

@Preview(
    name = "Reservation class info / F10 absent / Student",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun ReservationClassInfoSectionPreview_F10Absent_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationClassInfoSection(model = ReservationDetailPreviewFixture.absent)
    }
}
