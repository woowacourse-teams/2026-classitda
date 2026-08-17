package com.classitda.feature.student.myschedule.component.list

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_status_confirmed
import classitda.shared.generated.resources.my_schedule_status_confirmed_mark
import classitda.shared.generated.resources.my_schedule_status_waitlist
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleCardUiModel
import com.classitda.feature.student.myschedule.preview.MyScheduleUpcomingPreviewFixture

@Composable
internal fun UpcomingScheduleCard(
    item: UpcomingScheduleCardUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScheduleSummaryCard(
        scheduleLabel = item.timeRangeLabel,
        title = item.title,
        instructorName = item.instructorName,
        memo = item.memo,
        onClick = onClick,
        modifier = modifier,
        statusContent = { UpcomingScheduleStatus(item = item) },
    )
}

@Composable
private fun UpcomingScheduleStatus(item: UpcomingScheduleCardUiModel) {
    when (item) {
        is UpcomingScheduleCardUiModel.ConfirmedReservation -> {
            ScheduleStatusLabel(
                label = Res.string.my_schedule_status_confirmed,
                contentColor = StuColors.Green,
                mark = Res.string.my_schedule_status_confirmed_mark,
            )
        }

        is UpcomingScheduleCardUiModel.Waitlisted -> {
            ScheduleStatusLabel(
                label = Res.string.my_schedule_status_waitlist,
                contentColor = StuColors.Orange,
            )
        }
    }
}

@Preview(
    name = "Confirmed · Student · Default",
    group = "Component/MySchedule/UpcomingCard",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun UpcomingScheduleCardPreview_Confirmed_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        UpcomingScheduleCard(
            item = MyScheduleUpcomingPreviewFixture.confirmedReservation,
            onClick = {},
            modifier = Modifier.padding(AppSpacing.screenPadding),
        )
    }
}

@Preview(
    name = "Waitlist · Student · Default",
    group = "Component/MySchedule/UpcomingCard",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun UpcomingScheduleCardPreview_Waitlist_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        UpcomingScheduleCard(
            item = MyScheduleUpcomingPreviewFixture.waitlisted,
            onClick = {},
            modifier = Modifier.padding(AppSpacing.screenPadding),
        )
    }
}
