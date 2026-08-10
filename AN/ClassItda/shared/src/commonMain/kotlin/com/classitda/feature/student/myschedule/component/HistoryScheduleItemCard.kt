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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.contract.HistoryScheduleItemUiModel
import com.classitda.feature.student.myschedule.contract.HistoryScheduleStatusUiModel
import com.classitda.feature.student.myschedule.preview.myScheduleHistoryPreviewItems

@Composable
internal fun HistoryScheduleItemCard(
    item: HistoryScheduleItemUiModel,
    modifier: Modifier = Modifier,
) {
    val typography = appTypography()
    val containerColor =
        if (item.status == HistoryScheduleStatusUiModel.RESERVATION_CANCELED) {
            StuColors.SurfaceVariant
        } else {
            StuColors.Surface
        }

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardItemVerticalGap),
        ) {
            Text(
                text = item.dateTime.dateTimeLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                style = typography.bodyMedium,
                color = StuColors.TextSecondary,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ScheduleItemDetails(
                    item = item,
                    modifier = Modifier.weight(1f),
                )

                ScheduleStatusChip(
                    type =
                        when (item.status) {
                            HistoryScheduleStatusUiModel.COMPLETED -> {
                                ScheduleStatusChipType.COMPLETED
                            }

                            HistoryScheduleStatusUiModel.RESERVATION_CANCELED -> {
                                ScheduleStatusChipType.RESERVATION_CANCELED
                            }
                        },
                    modifier = Modifier.padding(start = AppSpacing.cardItemHorizontalGap),
                )
            }
        }
    }
}

@Preview(
    name = "Card · Completed · Student",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun HistoryScheduleItemCardPreview_Completed_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        HistoryScheduleItemCard(
            item = myScheduleHistoryPreviewItems().first(),
            modifier = Modifier.padding(AppSpacing.screenPadding),
        )
    }
}

@Preview(
    name = "Card · Reservation canceled · Student",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
)
@Composable
private fun HistoryScheduleItemCardPreview_ReservationCanceled_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        HistoryScheduleItemCard(
            item = myScheduleHistoryPreviewItems().last(),
            modifier = Modifier.padding(AppSpacing.screenPadding),
        )
    }
}
