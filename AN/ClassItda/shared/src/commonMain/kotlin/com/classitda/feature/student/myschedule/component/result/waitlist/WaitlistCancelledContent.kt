package com.classitda.feature.student.myschedule.component.result.waitlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import classitda.shared.generated.resources.ic_event_busy
import classitda.shared.generated.resources.my_schedule_cancelled_at
import classitda.shared.generated.resources.my_schedule_class_information
import classitda.shared.generated.resources.my_schedule_class_name
import classitda.shared.generated.resources.my_schedule_date_time
import classitda.shared.generated.resources.my_schedule_instructor
import classitda.shared.generated.resources.my_schedule_instructor_name
import classitda.shared.generated.resources.my_schedule_location
import classitda.shared.generated.resources.my_schedule_waitlist_auto_released
import classitda.shared.generated.resources.my_schedule_waitlist_cancellation_history
import classitda.shared.generated.resources.my_schedule_waitlist_cancelled_description
import classitda.shared.generated.resources.my_schedule_waitlist_cancelled_position
import classitda.shared.generated.resources.my_schedule_waitlist_cancelled_title
import classitda.shared.generated.resources.my_schedule_waitlist_number
import classitda.shared.generated.resources.my_schedule_waitlist_progress_status
import classitda.shared.generated.resources.my_schedule_waitlist_ticket_not_deducted
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.component.result.common.MyScheduleResultInformationRow
import com.classitda.feature.student.myschedule.component.result.common.MyScheduleResultSectionTitle
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleItemUiModel
import com.classitda.feature.student.myschedule.preview.waitlistCancelledPreviewFixture
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WaitlistCancelledContent(
    waitlist: UpcomingScheduleItemUiModel.Waitlist,
    dateLabel: String,
    timeRangeLabel: String,
    cancelledAtLabel: String,
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
            WaitlistCancelledHero()
        }
        item {
            WaitlistCancelledClassInformation(
                waitlist = waitlist,
                dateLabel = dateLabel,
                timeRangeLabel = timeRangeLabel,
            )
        }
        item {
            WaitlistCancellationHistory(
                position = waitlist.position,
                cancelledAtLabel = cancelledAtLabel,
            )
        }
    }
}

@Composable
private fun WaitlistCancelledHero() {
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
        Box(
            modifier =
                Modifier
                    .background(
                        color = StuColors.SurfaceVariant,
                        shape = AppShape.Pill,
                    ).padding(AppSpacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_event_busy),
                contentDescription = null,
                tint = StuColors.TextSecondary,
            )
        }
        Text(
            text = stringResource(Res.string.my_schedule_waitlist_cancelled_title),
            modifier = Modifier.semantics { heading() },
            style = typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.my_schedule_waitlist_cancelled_description),
            style = typography.bodyMedium,
            color = StuColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun WaitlistCancelledClassInformation(
    waitlist: UpcomingScheduleItemUiModel.Waitlist,
    dateLabel: String,
    timeRangeLabel: String,
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
            text = stringResource(Res.string.my_schedule_class_information),
        )
        MyScheduleResultInformationRow(
            label = stringResource(Res.string.my_schedule_class_name),
            value = waitlist.title,
        )
        MyScheduleResultInformationRow(
            label = stringResource(Res.string.my_schedule_instructor),
            value = stringResource(Res.string.my_schedule_instructor_name, waitlist.instructorName),
        )
        MyScheduleResultInformationRow(
            label = stringResource(Res.string.my_schedule_date_time),
            value = dateLabel,
            supportingValue = timeRangeLabel,
        )
        MyScheduleResultInformationRow(
            label = stringResource(Res.string.my_schedule_location),
            value = waitlist.locationLabel,
        )
    }
}

@Composable
private fun WaitlistCancellationHistory(
    position: Int,
    cancelledAtLabel: String,
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
            text = stringResource(Res.string.my_schedule_waitlist_cancellation_history),
        )
        MyScheduleResultInformationRow(
            label = stringResource(Res.string.my_schedule_waitlist_number),
            value = stringResource(Res.string.my_schedule_waitlist_cancelled_position, position),
        )
        MyScheduleResultInformationRow(
            label = stringResource(Res.string.my_schedule_cancelled_at),
            value = cancelledAtLabel,
        )
        MyScheduleResultInformationRow(
            label = stringResource(Res.string.my_schedule_waitlist_progress_status),
            value = stringResource(Res.string.my_schedule_waitlist_auto_released),
            supportingValue = stringResource(Res.string.my_schedule_waitlist_ticket_not_deducted),
            valueColor = StuColors.TextSecondary,
            supportingValueColor = StuColors.TextTertiary,
        )
    }
}

@Preview(
    name = "Waitlist cancelled content · Student · Default",
    group = "Component/MySchedule",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 660,
)
@Composable
private fun WaitlistCancelledContentPreview_Success_Student_Default() {
    val fixture = waitlistCancelledPreviewFixture()

    AppTheme(theme = ThemeType.STUDENT) {
        WaitlistCancelledContent(
            waitlist = fixture.item,
            dateLabel = fixture.dateLabel,
            timeRangeLabel = fixture.timeRangeLabel,
            cancelledAtLabel = fixture.cancelledAtLabel,
        )
    }
}
