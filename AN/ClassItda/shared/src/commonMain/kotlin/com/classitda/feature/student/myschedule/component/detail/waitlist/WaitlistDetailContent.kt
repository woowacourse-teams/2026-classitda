package com.classitda.feature.student.myschedule.component.detail.waitlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_calendar_today
import classitda.shared.generated.resources.ic_location_on
import classitda.shared.generated.resources.ic_schedule
import classitda.shared.generated.resources.my_schedule_bullet
import classitda.shared.generated.resources.my_schedule_cancel_waitlist
import classitda.shared.generated.resources.my_schedule_detail_time_with_duration
import classitda.shared.generated.resources.my_schedule_instructor_name
import classitda.shared.generated.resources.my_schedule_waitlist_cancellation_guide
import classitda.shared.generated.resources.my_schedule_waitlist_confirmation_cancellation_deadline
import classitda.shared.generated.resources.my_schedule_waitlist_confirmation_cancellation_policy
import classitda.shared.generated.resources.my_schedule_waitlist_date
import classitda.shared.generated.resources.my_schedule_waitlist_location
import classitda.shared.generated.resources.my_schedule_waitlist_position_description
import classitda.shared.generated.resources.my_schedule_waitlist_position_title
import classitda.shared.generated.resources.my_schedule_waitlist_position_value
import classitda.shared.generated.resources.my_schedule_waitlist_time
import com.classitda.core.designsystem.AppColor
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.component.common.MyScheduleWarningButton
import com.classitda.feature.student.myschedule.component.common.ScheduleStatusChip
import com.classitda.feature.student.myschedule.component.common.ScheduleStatusChipType
import com.classitda.feature.student.myschedule.contract.ScheduleCancellationAvailabilityUiModel
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleItemUiModel
import com.classitda.feature.student.myschedule.preview.waitlistDetailPreviewFixture
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WaitlistDetailContent(
    item: UpcomingScheduleItemUiModel.Waitlist,
    detailDateLabel: String,
    detailTimeRangeLabel: String,
    durationMinutes: Int,
    confirmedReservationCancellationDeadlineHours: Int,
    onCancelWaitlist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background),
    ) {
        item {
            WaitlistOverviewSection(
                item = item,
                detailDateLabel = detailDateLabel,
                detailTimeRangeLabel = detailTimeRangeLabel,
                durationMinutes = durationMinutes,
            )
        }
        item {
            WaitlistCancellationSection(
                deadlineHours = confirmedReservationCancellationDeadlineHours,
                isCancellationAvailable =
                    item.cancellation is ScheduleCancellationAvailabilityUiModel.Available,
                onCancelWaitlist = onCancelWaitlist,
            )
        }
    }
}

@Composable
private fun WaitlistOverviewSection(
    item: UpcomingScheduleItemUiModel.Waitlist,
    detailDateLabel: String,
    detailTimeRangeLabel: String,
    durationMinutes: Int,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.Surface)
                .padding(
                    horizontal = AppSpacing.screenPadding,
                    vertical = AppSpacing.xxl,
                ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionGap),
    ) {
        ScheduleStatusChip(type = ScheduleStatusChipType.WAITLIST)
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Text(
                text = item.title,
                style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = StuColors.TextPrimary,
            )
            Text(
                text = stringResource(Res.string.my_schedule_instructor_name, item.instructorName),
                style = appTypography().bodyLarge,
                color = StuColors.TextSecondary,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
            WaitlistDetailInformationRow(
                icon = Res.drawable.ic_calendar_today,
                label = stringResource(Res.string.my_schedule_waitlist_date),
                value = detailDateLabel,
            )
            WaitlistDetailInformationRow(
                icon = Res.drawable.ic_schedule,
                label = stringResource(Res.string.my_schedule_waitlist_time),
                value =
                    stringResource(
                        Res.string.my_schedule_detail_time_with_duration,
                        detailTimeRangeLabel,
                        durationMinutes,
                    ),
            )
            WaitlistDetailInformationRow(
                icon = Res.drawable.ic_location_on,
                label = stringResource(Res.string.my_schedule_waitlist_location),
                value = item.locationLabel,
            )
        }
        WaitlistPositionCard(position = item.position)
    }
}

@Composable
private fun WaitlistDetailInformationRow(
    icon: DrawableResource,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = AppShape.Card,
            color = StuColors.SurfaceVariant,
        ) {
            Box(modifier = Modifier.padding(AppSpacing.md)) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(AppSpacing.xl),
                    tint = StuColors.TextSecondary,
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                text = label,
                style = appTypography().bodyMedium,
                color = StuColors.TextSecondary,
            )
            Text(
                text = value,
                style = appTypography().titleMedium.copy(fontWeight = FontWeight.Bold),
                color = StuColors.TextPrimary,
            )
        }
    }
}

@Composable
private fun WaitlistPositionCard(position: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShape.Card,
        color = StuColors.SurfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.my_schedule_waitlist_position_title),
                    style = appTypography().titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = StuColors.TextPrimary,
                )
                Text(
                    text = stringResource(Res.string.my_schedule_waitlist_position_value, position),
                    style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = AppColor.DarkOrange,
                )
            }
            Text(
                text = stringResource(Res.string.my_schedule_waitlist_position_description),
                style = appTypography().bodyMedium,
                color = StuColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun WaitlistCancellationSection(
    deadlineHours: Int,
    isCancellationAvailable: Boolean,
    onCancelWaitlist: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.Background)
                .padding(horizontal = AppSpacing.screenPadding)
                .padding(
                    top = AppSpacing.xxxl,
                    bottom = AppSpacing.screenPadding,
                ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        Text(
            text = stringResource(Res.string.my_schedule_waitlist_cancellation_guide),
            style = appTypography().titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = StuColors.TextSecondary,
        )
        WaitlistCancellationBullet(
            text =
                stringResource(
                    Res.string.my_schedule_waitlist_confirmation_cancellation_deadline,
                    deadlineHours,
                ),
        )
        WaitlistCancellationBullet(
            text = stringResource(Res.string.my_schedule_waitlist_confirmation_cancellation_policy),
        )
        MyScheduleWarningButton(
            text = stringResource(Res.string.my_schedule_cancel_waitlist),
            onClick = onCancelWaitlist,
            modifier = Modifier.padding(top = AppSpacing.lg),
            enabled = isCancellationAvailable,
        )
    }
}

@Composable
private fun WaitlistCancellationBullet(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = stringResource(Res.string.my_schedule_bullet),
            style = appTypography().bodyMedium,
            color = StuColors.TextTertiary,
        )
        Text(
            text = text,
            style = appTypography().bodyMedium,
            color = StuColors.TextTertiary,
        )
    }
}

@Preview(
    name = "Waitlist detail content / Student / Default",
    group = "Component/MySchedule",
    showBackground = true,
    locale = "ko",
    widthDp = 390,
    heightDp = 788,
)
@Composable
private fun WaitlistDetailContentPreview_Default_Student() {
    val fixture = waitlistDetailPreviewFixture()

    AppTheme(theme = ThemeType.STUDENT) {
        WaitlistDetailContent(
            item = fixture.item,
            detailDateLabel = fixture.dateLabel,
            detailTimeRangeLabel = fixture.timeRangeLabel,
            durationMinutes = fixture.durationMinutes,
            confirmedReservationCancellationDeadlineHours = fixture.deadlineHours,
            onCancelWaitlist = {},
        )
    }
}
