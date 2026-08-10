package com.classitda.feature.student.myschedule.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.student.myschedule.contract.HistoryScheduleItemUiModel
import com.classitda.feature.student.myschedule.preview.myScheduleHistoryPreviewItems

@Composable
internal fun HistoryScheduleList(
    items: List<HistoryScheduleItemUiModel>,
    state: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier,
) {
    val itemsByMonth = items.groupBy { it.dateTime.monthLabel }

    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding =
            PaddingValues(
                horizontal = AppSpacing.screenPadding,
                vertical = AppSpacing.sectionGap,
            ),
    ) {
        itemsByMonth.entries.forEachIndexed { monthIndex, (monthLabel, monthItems) ->
            item(key = "month-$monthLabel") {
                Text(
                    text = monthLabel,
                    modifier = Modifier.padding(bottom = AppSpacing.cardGap),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            itemsIndexed(
                items = monthItems,
                key = { _, item -> item.id.value },
            ) { itemIndex, item ->
                val isLastItemOfMonth = itemIndex == monthItems.lastIndex
                val isLastMonth = monthIndex == itemsByMonth.size - 1
                val itemModifier =
                    when {
                        isLastItemOfMonth && !isLastMonth -> Modifier.padding(bottom = AppSpacing.sectionGap)
                        !isLastItemOfMonth -> Modifier.padding(bottom = AppSpacing.cardGap)
                        else -> Modifier
                    }
                HistoryScheduleItemCard(
                    item = item,
                    modifier = itemModifier,
                )
            }
        }
    }
}

@Preview(
    name = "History list · Student · Default",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
    heightDp = 652,
)
@Composable
private fun HistoryScheduleListPreview_History_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        Surface(color = MaterialTheme.colorScheme.background) {
            HistoryScheduleList(items = myScheduleHistoryPreviewItems())
        }
    }
}
