package com.classitda.feature.student.myschedule.component.list

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_status_absent
import classitda.shared.generated.resources.my_schedule_status_attended
import classitda.shared.generated.resources.my_schedule_status_completed_mark
import classitda.shared.generated.resources.my_schedule_status_reservation_canceled
import classitda.shared.generated.resources.my_schedule_status_reservation_canceled_mark
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.student.myschedule.contract.UsageHistoryCardUiModel
import com.classitda.feature.student.myschedule.contract.UsageHistoryStatusUiModel
import com.classitda.feature.student.myschedule.preview.MyScheduleUsageHistoryPreviewFixture

@Composable
internal fun UsageHistoryCard(
    item: UsageHistoryCardUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScheduleSummaryCard(
        scheduleLabel = item.dateTimeLabel,
        title = item.title,
        instructorName = item.instructorName,
        onClick = onClick,
        modifier = modifier,
        statusContent = { UsageHistoryStatus(status = item.status) },
    )
}

@Composable
private fun UsageHistoryStatus(status: UsageHistoryStatusUiModel) {
    when (status) {
        UsageHistoryStatusUiModel.ATTENDED -> {
            ScheduleStatusLabel(
                label = Res.string.my_schedule_status_attended,
                contentColor = StuColors.TextSecondary,
                mark = Res.string.my_schedule_status_completed_mark,
            )
        }

        UsageHistoryStatusUiModel.ABSENT -> {
            ScheduleStatusLabel(
                label = Res.string.my_schedule_status_absent,
                contentColor = StuColors.TextSecondary,
                mark = Res.string.my_schedule_status_reservation_canceled_mark,
            )
        }

        UsageHistoryStatusUiModel.RESERVATION_CANCELLED -> {
            ScheduleStatusLabel(
                label = Res.string.my_schedule_status_reservation_canceled,
                contentColor = StuColors.Red,
                mark = Res.string.my_schedule_status_reservation_canceled_mark,
            )
        }
    }
}

@Preview(
    name = "Attended · Student · Default",
    group = "Component/MySchedule/UsageHistoryCard",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun UsageHistoryCardPreview_Attended_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        UsageHistoryCard(
            item = MyScheduleUsageHistoryPreviewFixture.attended,
            onClick = {},
            modifier = Modifier.padding(AppSpacing.screenPadding),
        )
    }
}

@Preview(
    name = "Absent · Student · Default",
    group = "Component/MySchedule/UsageHistoryCard",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun UsageHistoryCardPreview_Absent_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        UsageHistoryCard(
            item = MyScheduleUsageHistoryPreviewFixture.absent,
            onClick = {},
            modifier = Modifier.padding(AppSpacing.screenPadding),
        )
    }
}

@Preview(
    name = "Reservation cancelled · Student · Default",
    group = "Component/MySchedule/UsageHistoryCard",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun UsageHistoryCardPreview_ReservationCancelled_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        UsageHistoryCard(
            item = MyScheduleUsageHistoryPreviewFixture.reservationCancelled,
            onClick = {},
            modifier = Modifier.padding(AppSpacing.screenPadding),
        )
    }
}
