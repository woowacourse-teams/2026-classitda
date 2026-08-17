package com.classitda.feature.student.myschedule.component.result.reservation

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
import classitda.shared.generated.resources.my_schedule_refund_restoration_status
import classitda.shared.generated.resources.my_schedule_reservation_cancelled_description
import classitda.shared.generated.resources.my_schedule_reservation_cancelled_title
import classitda.shared.generated.resources.my_schedule_ticket_restoration_completed
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.component.result.common.MyScheduleResultInformationRow
import com.classitda.feature.student.myschedule.component.result.common.MyScheduleResultSectionTitle
import com.classitda.feature.student.myschedule.contract.ReservationCancellationResultUiModel
import com.classitda.feature.student.myschedule.preview.ReservationDetailPreviewFixture
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReservationCancellationCompletedContent(
    result: ReservationCancellationResultUiModel,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
        item { ReservationCancellationCompletedHero() }
        item { ReservationCancellationCompletedClassInformation(result = result) }
        item { ReservationCancellationCompletedHistory(result = result) }
    }
}

@Composable
private fun ReservationCancellationCompletedHero() {
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
            text = stringResource(Res.string.my_schedule_reservation_cancelled_title),
            modifier = Modifier.semantics { heading() },
            style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.my_schedule_reservation_cancelled_description),
            style = appTypography().bodyMedium,
            color = StuColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ReservationCancellationCompletedClassInformation(result: ReservationCancellationResultUiModel) {
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
            value = result.classInfo.instructorName,
        )
        MyScheduleResultInformationRow(
            label = stringResource(Res.string.my_schedule_date_time),
            value = result.classInfo.dateLabel,
            supportingValue = result.classInfo.timeRangeLabel,
        )
    }
}

@Composable
private fun ReservationCancellationCompletedHistory(result: ReservationCancellationResultUiModel) {
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
            label = stringResource(Res.string.my_schedule_refund_restoration_status),
            value =
                stringResource(
                    Res.string.my_schedule_ticket_restoration_completed,
                    result.restoredPassUses,
                ),
            valueColor = StuColors.Green,
        )
    }
}

@Preview(
    name = "F05 reservation cancellation completed content / Student",
    group = "Component/MySchedule",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 660,
)
@Composable
private fun ReservationCancellationCompletedContentPreview_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        ReservationCancellationCompletedContent(
            result = ReservationDetailPreviewFixture.cancellationCompleted,
        )
    }
}
