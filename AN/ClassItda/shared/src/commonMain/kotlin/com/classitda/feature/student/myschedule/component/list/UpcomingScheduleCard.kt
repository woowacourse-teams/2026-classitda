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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_separator
import classitda.shared.generated.resources.my_schedule_status_confirmed
import classitda.shared.generated.resources.my_schedule_status_confirmed_mark
import classitda.shared.generated.resources.my_schedule_status_waitlist
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleCardUiModel
import com.classitda.feature.student.myschedule.preview.MyScheduleUpcomingPreviewFixture
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun UpcomingScheduleCard(
    item: UpcomingScheduleCardUiModel,
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
                text = item.timeRangeLabel,
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
                    style = typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                UpcomingScheduleStatus(item = item)
            }
            UpcomingScheduleCardDetails(item = item)
        }
    }
}

@Composable
private fun UpcomingScheduleStatus(
    item: UpcomingScheduleCardUiModel,
    modifier: Modifier = Modifier,
) {
    val typography = appTypography()
    val mark: StringResource?
    val label: StringResource
    val contentColor =
        when (item) {
            is UpcomingScheduleCardUiModel.ConfirmedReservation -> {
                mark = Res.string.my_schedule_status_confirmed_mark
                label = Res.string.my_schedule_status_confirmed
                StuColors.Green
            }

            is UpcomingScheduleCardUiModel.Waitlisted -> {
                mark = null
                label = Res.string.my_schedule_status_waitlist
                StuColors.Orange
            }
        }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardItemHorizontalGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (mark != null) {
            Text(
                text = stringResource(mark),
                style = typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = contentColor,
            )
        }
        Text(
            text = stringResource(label),
            style = typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = contentColor,
        )
    }
}

@Composable
private fun UpcomingScheduleCardDetails(
    item: UpcomingScheduleCardUiModel,
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
