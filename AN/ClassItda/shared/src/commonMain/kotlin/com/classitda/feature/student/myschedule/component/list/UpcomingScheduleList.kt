package com.classitda.feature.student.myschedule.component.list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleItemUiModel
import com.classitda.feature.student.myschedule.preview.myScheduleReservationsPreviewItems

@Composable
internal fun UpcomingScheduleList(
    items: List<UpcomingScheduleItemUiModel>,
    state: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier,
) {
    val typography = appTypography()
    val itemsByDate = items.groupBy { it.dateTime.sectionDateLabel }

    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding =
            PaddingValues(
                horizontal = AppSpacing.screenPadding,
                vertical = AppSpacing.sectionGap,
            ),
    ) {
        itemsByDate.entries.forEachIndexed { dateIndex, (dateLabel, dateItems) ->
            item(key = "date-$dateLabel") {
                Text(
                    text = dateLabel,
                    modifier = Modifier.padding(bottom = AppSpacing.cardGap),
                    style = typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = StuColors.TextPrimary,
                )
            }
            itemsIndexed(
                items = dateItems,
                key = { _, item -> item.id.value },
            ) { itemIndex, item ->
                val isLastItemOfDate = itemIndex == dateItems.lastIndex
                val isLastDate = dateIndex == itemsByDate.size - 1
                val itemModifier =
                    when {
                        isLastItemOfDate && !isLastDate -> Modifier.padding(bottom = AppSpacing.sectionGap)
                        !isLastItemOfDate -> Modifier.padding(bottom = AppSpacing.cardGap)
                        else -> Modifier
                    }
                ScheduleItemCard(
                    item = item,
                    modifier = itemModifier,
                )
            }
        }
    }
}

@Preview(
    name = "List / Reservations / Student",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
    heightDp = 652,
)
@Composable
private fun `UpcomingScheduleListPreview_Reservations_STUDENT_Default`() {
    AppTheme(theme = ThemeType.STUDENT) {
        UpcomingScheduleList(
            items = myScheduleReservationsPreviewItems(),
        )
    }
}
