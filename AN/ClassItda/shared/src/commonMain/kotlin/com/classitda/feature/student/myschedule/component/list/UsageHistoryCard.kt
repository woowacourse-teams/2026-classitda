package com.classitda.feature.student.myschedule.component.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_separator
import classitda.shared.generated.resources.my_schedule_status_absent
import classitda.shared.generated.resources.my_schedule_status_attended
import classitda.shared.generated.resources.my_schedule_status_completed_mark
import classitda.shared.generated.resources.my_schedule_status_reservation_canceled
import classitda.shared.generated.resources.my_schedule_status_reservation_canceled_mark
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.contract.UsageHistoryCardUiModel
import com.classitda.feature.student.myschedule.contract.UsageHistoryStatusUiModel
import com.classitda.feature.student.myschedule.preview.MyScheduleUsageHistoryPreviewFixture
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun UsageHistoryCard(
    item: UsageHistoryCardUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = appTypography()

    OutlinedCard(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {},
        shape = AppShape.Card,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardItemVerticalGap),
        ) {
            Text(
                text = item.dateTimeLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardItemHorizontalGap),
            ) {
                Text(
                    text = item.title,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                UsageHistoryStatus(status = item.status)
            }
            UsageHistoryCardDetails(item = item)
        }
    }
}

@Composable
private fun UsageHistoryStatus(
    status: UsageHistoryStatusUiModel,
    modifier: Modifier = Modifier,
) {
    val typography = appTypography()
    val mark: StringResource
    val label: StringResource
    val contentColor: Color

    when (status) {
        UsageHistoryStatusUiModel.ATTENDED -> {
            mark = Res.string.my_schedule_status_completed_mark
            label = Res.string.my_schedule_status_attended
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        }

        UsageHistoryStatusUiModel.ABSENT -> {
            mark = Res.string.my_schedule_status_reservation_canceled_mark
            label = Res.string.my_schedule_status_absent
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        }

        UsageHistoryStatusUiModel.RESERVATION_CANCELLED -> {
            mark = Res.string.my_schedule_status_reservation_canceled_mark
            label = Res.string.my_schedule_status_reservation_canceled
            contentColor = MaterialTheme.colorScheme.error
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardItemHorizontalGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(mark),
            style = typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = contentColor,
        )
        Text(
            text = stringResource(label),
            style = typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = contentColor,
        )
    }
}

@Composable
private fun UsageHistoryCardDetails(
    item: UsageHistoryCardUiModel,
    modifier: Modifier = Modifier,
) {
    val typography = appTypography()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardItemHorizontalGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.instructorName,
            style = typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        item.memo?.let { memo ->
            Text(
                text = stringResource(Res.string.my_schedule_separator),
                style = typography.bodySmall,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            Text(
                text = memo,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
