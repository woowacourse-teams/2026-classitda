package com.classitda.feature.student.myschedule.component.list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.student.myschedule.ReservationId
import com.classitda.feature.student.myschedule.contract.UsageHistoryMonthSectionUiModel
import com.classitda.feature.student.myschedule.preview.MyScheduleUsageHistoryPreviewFixture

@Composable
internal fun UsageHistorySectionList(
    sections: List<UsageHistoryMonthSectionUiModel>,
    onOpenReservation: (ReservationId) -> Unit,
    state: LazyListState = rememberLazyListState(),
    modifier: Modifier = Modifier,
) {
    val typography = appTypography()

    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding =
            PaddingValues(
                horizontal = AppSpacing.screenPadding,
                vertical = AppSpacing.sectionGap,
            ),
    ) {
        sections.forEachIndexed { sectionIndex, section ->
            item(key = "usage-history-month-${section.monthLabel}") {
                Text(
                    text = section.monthLabel,
                    modifier =
                        Modifier
                            .padding(bottom = AppSpacing.cardGap)
                            .semantics { heading() },
                    style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            itemsIndexed(
                items = section.items,
                key = { _, item -> "usage-history:${item.reservationId.value}" },
            ) { itemIndex, item ->
                val isLastItemOfSection = itemIndex == section.items.lastIndex
                val isLastSection = sectionIndex == sections.lastIndex
                val itemModifier =
                    when {
                        isLastItemOfSection && !isLastSection -> {
                            Modifier.padding(bottom = AppSpacing.sectionGap)
                        }

                        !isLastItemOfSection -> {
                            Modifier.padding(bottom = AppSpacing.cardGap)
                        }

                        else -> {
                            Modifier
                        }
                    }

                UsageHistoryCard(
                    item = item,
                    onClick = { onOpenReservation(item.reservationId) },
                    modifier = itemModifier,
                )
            }
        }
    }
}

@Preview(
    name = "Content · Student · Default",
    group = "Component/MySchedule/UsageHistoryList",
    showBackground = true,
    widthDp = 390,
    heightDp = 772,
)
@Composable
private fun UsageHistorySectionListPreview_Content_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        UsageHistorySectionList(
            sections = MyScheduleUsageHistoryPreviewFixture.sections,
            onOpenReservation = {},
        )
    }
}
