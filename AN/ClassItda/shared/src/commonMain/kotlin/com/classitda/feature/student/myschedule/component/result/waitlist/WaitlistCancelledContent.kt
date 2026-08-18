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
import classitda.shared.generated.resources.my_schedule_cancellation_history
import classitda.shared.generated.resources.my_schedule_cancelled_at
import classitda.shared.generated.resources.my_schedule_class_information
import classitda.shared.generated.resources.my_schedule_class_name
import classitda.shared.generated.resources.my_schedule_date_time
import classitda.shared.generated.resources.my_schedule_instructor
import classitda.shared.generated.resources.my_schedule_waitlist_cancelled_description
import classitda.shared.generated.resources.my_schedule_waitlist_cancelled_position
import classitda.shared.generated.resources.my_schedule_waitlist_cancelled_title
import classitda.shared.generated.resources.my_schedule_waitlist_number
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.component.result.common.MyScheduleResultInformationRow
import com.classitda.feature.student.myschedule.component.result.common.MyScheduleResultSectionTitle
import com.classitda.feature.student.myschedule.contract.WaitlistCancellationResultUiModel
import com.classitda.feature.student.myschedule.preview.WaitlistCancellationResultPreviewFixture
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WaitlistCancelledContent(
    result: WaitlistCancellationResultUiModel,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
        item { WaitlistCancelledHero() }
        item { WaitlistCancelledClassInformation(result = result) }
        item { WaitlistCancellationHistory(result = result) }
    }
}

@Composable
private fun WaitlistCancelledHero() {
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
            style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.my_schedule_waitlist_cancelled_description),
            style = appTypography().bodyMedium,
            color = StuColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun WaitlistCancelledClassInformation(result: WaitlistCancellationResultUiModel) {
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
            value = result.title,
        )
        MyScheduleResultInformationRow(
            label = stringResource(Res.string.my_schedule_instructor),
            value = result.instructorName,
        )
        MyScheduleResultInformationRow(
            label = stringResource(Res.string.my_schedule_date_time),
            value = result.dateLabel,
            supportingValue = result.timeRangeLabel,
        )
    }
}

@Composable
private fun WaitlistCancellationHistory(result: WaitlistCancellationResultUiModel) {
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
            value = result.cancelledAtLabel,
        )
        MyScheduleResultInformationRow(
            label = stringResource(Res.string.my_schedule_waitlist_number),
            value =
                stringResource(
                    Res.string.my_schedule_waitlist_cancelled_position,
                    result.positionAtCancellation,
                ),
        )
    }
}

@Preview(
    name = "F08 waitlist cancellation completed content / Student",
    group = "Component/MySchedule/WaitlistCancellation",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 660,
)
@Composable
private fun WaitlistCancelledContentPreview_F08Completed_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        WaitlistCancelledContent(
            result = WaitlistCancellationResultPreviewFixture.completed,
        )
    }
}
