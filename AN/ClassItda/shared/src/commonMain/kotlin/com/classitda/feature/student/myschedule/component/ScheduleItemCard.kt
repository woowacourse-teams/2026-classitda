package com.classitda.feature.student.myschedule.component

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
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_open_detail_mark
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleItemUiModel
import com.classitda.feature.student.myschedule.preview.myScheduleReservationsPreviewItems
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ScheduleItemCard(
    item: UpcomingScheduleItemUiModel,
    modifier: Modifier = Modifier,
) {
    val typography = appTypography()

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(containerColor = StuColors.Surface),
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
                    text = item.dateTime.timeRangeLabel,
                    style = typography.bodyLarge,
                    color =
                        if (item is UpcomingScheduleItemUiModel.ConfirmedReservation) {
                            StuColors.PrimaryGreen
                        } else {
                            StuColors.TextSecondary
                        },
                )
                ScheduleItemDetails(item = item)
            }

            ScheduleStatusChip(
                type =
                    when (item) {
                        is UpcomingScheduleItemUiModel.ConfirmedReservation -> {
                            ScheduleStatusChipType.CONFIRMED_RESERVATION
                        }

                        is UpcomingScheduleItemUiModel.Waitlist -> {
                            ScheduleStatusChipType.WAITLIST
                        }
                    },
                modifier = Modifier.padding(start = AppSpacing.cardItemHorizontalGap),
            )
            Text(
                text = stringResource(Res.string.my_schedule_open_detail_mark),
                modifier = Modifier.padding(start = AppSpacing.cardItemHorizontalGap),
                style = typography.titleLarge,
                color = StuColors.Divider,
            )
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
            item = myScheduleReservationsPreviewItems().first(),
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
            item = myScheduleReservationsPreviewItems()[1],
            modifier = Modifier.padding(AppSpacing.screenPadding),
        )
    }
}
