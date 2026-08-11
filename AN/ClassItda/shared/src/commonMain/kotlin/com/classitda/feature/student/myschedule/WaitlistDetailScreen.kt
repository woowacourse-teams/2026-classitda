package com.classitda.feature.student.myschedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.student.myschedule.component.detail.waitlist.WaitlistDetailContent
import com.classitda.feature.student.myschedule.component.detail.waitlist.WaitlistDetailTopBar
import com.classitda.feature.student.myschedule.contract.ScheduleItemId
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleItemUiModel
import com.classitda.feature.student.myschedule.preview.waitlistDetailPreviewFixture

@Composable
fun WaitlistDetailScreen(
    item: UpcomingScheduleItemUiModel.Waitlist,
    detailDateLabel: String,
    detailTimeRangeLabel: String,
    durationMinutes: Int,
    confirmedReservationCancellationDeadlineHours: Int,
    onBack: () -> Unit,
    onCancelWaitlist: (ScheduleItemId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background),
    ) {
        WaitlistDetailTopBar(onBack = onBack)
        WaitlistDetailContent(
            item = item,
            detailDateLabel = detailDateLabel,
            detailTimeRangeLabel = detailTimeRangeLabel,
            durationMinutes = durationMinutes,
            confirmedReservationCancellationDeadlineHours =
            confirmedReservationCancellationDeadlineHours,
            onCancelWaitlist = { onCancelWaitlist(item.id) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(
    name = "Waitlist detail · Student · Default",
    group = "Screen/MySchedule",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun WaitlistDetailScreenPreview_Default_Student() {
    val fixture = waitlistDetailPreviewFixture()

    AppTheme(theme = ThemeType.STUDENT) {
        WaitlistDetailScreen(
            item = fixture.item,
            detailDateLabel = fixture.dateLabel,
            detailTimeRangeLabel = fixture.timeRangeLabel,
            durationMinutes = fixture.durationMinutes,
            confirmedReservationCancellationDeadlineHours = fixture.deadlineHours,
            onBack = {},
            onCancelWaitlist = {},
        )
    }
}
