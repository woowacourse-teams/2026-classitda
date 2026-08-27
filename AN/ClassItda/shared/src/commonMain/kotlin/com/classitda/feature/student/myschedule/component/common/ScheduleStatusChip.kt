package com.classitda.feature.student.myschedule.component.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_status_completed
import classitda.shared.generated.resources.my_schedule_status_completed_mark
import classitda.shared.generated.resources.my_schedule_status_confirmed
import classitda.shared.generated.resources.my_schedule_status_confirmed_mark
import classitda.shared.generated.resources.my_schedule_status_reservation_canceled
import classitda.shared.generated.resources.my_schedule_status_reservation_canceled_mark
import classitda.shared.generated.resources.my_schedule_status_waitlist
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal enum class ScheduleStatusChipType {
    CONFIRMED_RESERVATION,
    WAITLIST,
    COMPLETED,
    RESERVATION_CANCELED,
}

@Composable
internal fun ScheduleStatusChip(
    type: ScheduleStatusChipType,
    modifier: Modifier = Modifier,
    labelOverride: StringResource? = null,
) {
    val typography = appTypography()
    val label: StringResource
    val mark: StringResource?
    val showWaitlistMark: Boolean
    val containerColor: Color
    val contentColor: Color

    when (type) {
        ScheduleStatusChipType.CONFIRMED_RESERVATION -> {
            label = Res.string.my_schedule_status_confirmed
            mark = Res.string.my_schedule_status_confirmed_mark
            showWaitlistMark = false
            containerColor = StuColors.GreenLight
            contentColor = StuColors.Green
        }

        ScheduleStatusChipType.WAITLIST -> {
            label = Res.string.my_schedule_status_waitlist
            mark = null
            showWaitlistMark = true
            containerColor = StuColors.OrangeLight
            contentColor = StuColors.Orange
        }

        ScheduleStatusChipType.COMPLETED -> {
            label = Res.string.my_schedule_status_completed
            mark = Res.string.my_schedule_status_completed_mark
            showWaitlistMark = false
            containerColor = StuColors.Divider
            contentColor = StuColors.TextSecondary
        }

        ScheduleStatusChipType.RESERVATION_CANCELED -> {
            label = Res.string.my_schedule_status_reservation_canceled
            mark = Res.string.my_schedule_status_reservation_canceled_mark
            showWaitlistMark = false
            containerColor = StuColors.RedLight
            contentColor = StuColors.Red
        }
    }

    Surface(
        modifier = modifier,
        shape = AppShape.Pill,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = AppSpacing.chipHorizontalPadding,
                    vertical = AppSpacing.chipVerticalPadding,
                ),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.chipIconGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (mark != null) {
                Text(
                    text = stringResource(mark),
                    style = typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                )
            } else if (showWaitlistMark) {
                Box(
                    modifier =
                        Modifier
                            .size(AppSpacing.sm)
                            .background(contentColor, CircleShape),
                )
            }
            Text(
                text = stringResource(labelOverride ?: label),
                style = typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Preview(
    name = "Status chips / Reservations / Student",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun `ScheduleStatusChipPreview_Reservations_STUDENT_Default`() {
    AppTheme(theme = ThemeType.STUDENT) {
        Column(
            modifier = Modifier.padding(AppSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            ScheduleStatusChip(type = ScheduleStatusChipType.CONFIRMED_RESERVATION)
            ScheduleStatusChip(type = ScheduleStatusChipType.WAITLIST)
        }
    }
}

@Preview(
    name = "Status chips · History · Student",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun ScheduleStatusChipPreview_History_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        Column(
            modifier = Modifier.padding(AppSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            ScheduleStatusChip(type = ScheduleStatusChipType.COMPLETED)
            ScheduleStatusChip(type = ScheduleStatusChipType.RESERVATION_CANCELED)
        }
    }
}
