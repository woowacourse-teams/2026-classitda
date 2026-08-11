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
import com.classitda.feature.student.myschedule.component.result.common.MyScheduleResultActionSection
import com.classitda.feature.student.myschedule.component.result.common.MyScheduleResultTopBar
import com.classitda.feature.student.myschedule.component.result.waitlist.WaitlistCancelledContent
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleItemUiModel
import com.classitda.feature.student.myschedule.preview.waitlistCancelledPreviewFixture

@Composable
fun WaitlistCancelledScreen(
    waitlist: UpcomingScheduleItemUiModel.Waitlist,
    dateLabel: String,
    timeRangeLabel: String,
    cancelledAtLabel: String,
    onBack: () -> Unit,
    onBookAnotherClass: () -> Unit,
    onReturnToList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background),
    ) {
        MyScheduleResultTopBar(onBack = onBack)
        WaitlistCancelledContent(
            waitlist = waitlist,
            dateLabel = dateLabel,
            timeRangeLabel = timeRangeLabel,
            cancelledAtLabel = cancelledAtLabel,
            modifier = Modifier.weight(1f),
        )
        MyScheduleResultActionSection(
            onBookAnotherClass = onBookAnotherClass,
            onReturnToList = onReturnToList,
        )
    }
}

@Preview(
    name = "Waitlist cancelled · Student · Default",
    group = "Screen/MySchedule",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun WaitlistCancelledScreenPreview_Success_Student_Default() {
    val fixture = waitlistCancelledPreviewFixture()

    AppTheme(theme = ThemeType.STUDENT) {
        WaitlistCancelledScreen(
            waitlist = fixture.item,
            dateLabel = fixture.dateLabel,
            timeRangeLabel = fixture.timeRangeLabel,
            cancelledAtLabel = fixture.cancelledAtLabel,
            onBack = {},
            onBookAnotherClass = {},
            onReturnToList = {},
        )
    }
}
