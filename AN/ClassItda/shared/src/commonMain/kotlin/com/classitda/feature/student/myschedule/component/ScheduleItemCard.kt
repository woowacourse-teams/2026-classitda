package com.classitda.feature.student.myschedule.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_instructor_name
import classitda.shared.generated.resources.my_schedule_open_detail_mark
import classitda.shared.generated.resources.my_schedule_status_confirmed
import classitda.shared.generated.resources.my_schedule_status_confirmed_mark
import classitda.shared.generated.resources.my_schedule_status_waitlist
import com.classitda.core.designsystem.AppColor
import com.classitda.core.designsystem.AppColor.SecondaryOrange
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.student.myschedule.contract.ActiveScheduleItemUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleItemUiModel
import com.classitda.feature.student.myschedule.preview.myScheduleReservationsPreviewItems
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource


@Composable
internal fun ScheduleItemCard(
    item: ActiveScheduleItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardItemVerticalGap),
            ) {
                Text(
                    text = item.dateTime.timeLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color =
                        when (item) {
                            is ScheduleItemUiModel.ConfirmedReservation -> MaterialTheme.colorScheme.primary
                            is ScheduleItemUiModel.Waitlist -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardItemHorizontalGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.title,
                        modifier = Modifier.weight(1f, fill = false),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(Res.string.my_schedule_instructor_name, item.instructorName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (item.locationLabel.isNotBlank()) {
                    Text(
                        text = item.locationLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.width(AppSpacing.cardItemHorizontalGap))
            ScheduleStatusChip(item = item)
            Spacer(modifier = Modifier.width(AppSpacing.cardItemHorizontalGap))
            Text(
                text = stringResource(Res.string.my_schedule_open_detail_mark),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun ScheduleStatusChip(item: ActiveScheduleItemUiModel) {
    val label: StringResource
    val containerColor: Color
    val contentColor: Color

    when (item) {
        is ScheduleItemUiModel.ConfirmedReservation -> {
            label = Res.string.my_schedule_status_confirmed
            containerColor = MaterialTheme.colorScheme.primaryContainer
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        }

        is ScheduleItemUiModel.Waitlist -> {
            label = Res.string.my_schedule_status_waitlist
            containerColor = SecondaryOrange
            contentColor = AppColor.AccentOrange
        }
    }

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = AppSpacing.chipHorizontalPadding,
                    vertical = AppSpacing.chipVerticalPadding,
                ),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.chipIconGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (item) {
                is ScheduleItemUiModel.ConfirmedReservation -> {
                    Text(
                        text = stringResource(Res.string.my_schedule_status_confirmed_mark),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                }

                is ScheduleItemUiModel.Waitlist -> {
                    Box(
                        modifier =
                            Modifier
                                .size(AppSpacing.sm)
                                .background(AppColor.AccentOrange, CircleShape),
                    )
                }
            }
            Text(
                text = stringResource(label),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Preview(
    name = "Status chips / Reservations / Student",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun `ScheduleStatusChipPreview_Reservations_STUDENT_Default`() {
    AppTheme(theme = ThemeType.STUDENT) {
        Column(
            modifier = Modifier.padding(AppSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            ScheduleStatusChip(item = myScheduleReservationsPreviewItems.first())
            ScheduleStatusChip(item = myScheduleReservationsPreviewItems[1])
        }
    }
}

@Preview(
    name = "Card / Confirmed / Student",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun `ScheduleItemCardPreview_Confirmed_STUDENT_Default`() {
    AppTheme(theme = ThemeType.STUDENT) {
        ScheduleItemCard(
            item = myScheduleReservationsPreviewItems.first(),
            onClick = {},
            modifier = Modifier.padding(AppSpacing.screenPadding),
        )
    }
}

@Preview(
    name = "Card / Waitlist / Student",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun `ScheduleItemCardPreview_Waitlist_STUDENT_Default`() {
    AppTheme(theme = ThemeType.STUDENT) {
        ScheduleItemCard(
            item = myScheduleReservationsPreviewItems[1],
            onClick = {},
            modifier = Modifier.padding(AppSpacing.screenPadding),
        )
    }
}
