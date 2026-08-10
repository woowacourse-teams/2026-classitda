package com.classitda.feature.student.myschedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppColor.White
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.student.myschedule.component.HistoryScheduleList
import com.classitda.feature.student.myschedule.component.MyScheduleTabRow
import com.classitda.feature.student.myschedule.component.MyScheduleTopBar
import com.classitda.feature.student.myschedule.component.UpcomingScheduleList
import com.classitda.feature.student.myschedule.contract.HistoryScheduleItemUiModel
import com.classitda.feature.student.myschedule.contract.MyScheduleTab
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleItemUiModel
import com.classitda.feature.student.myschedule.preview.myScheduleHistoryPreviewItems
import com.classitda.feature.student.myschedule.preview.myScheduleReservationsPreviewItems

@Composable
fun MyScheduleScreen(
    selectedTab: MyScheduleTab,
    upcomingItems: List<UpcomingScheduleItemUiModel>,
    historyItems: List<HistoryScheduleItemUiModel>,
    modifier: Modifier = Modifier,
) {
    val upcomingListState = rememberLazyListState()
    val historyListState = rememberLazyListState()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(White),
    ) {
        MyScheduleTopBar()
        MyScheduleTabRow(
            selectedTab = selectedTab,
        )

        when (selectedTab) {
            MyScheduleTab.UPCOMING -> {
                UpcomingTabContent(
                    items = upcomingItems,
                    listState = upcomingListState,
                    modifier = Modifier.weight(1f),
                )
            }

            MyScheduleTab.HISTORY -> {
                HistoryTabContent(
                    items = historyItems,
                    listState = historyListState,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun UpcomingTabContent(
    items: List<UpcomingScheduleItemUiModel>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    UpcomingScheduleList(
        items = items,
        state = listState,
        modifier = modifier,
    )
}

@Composable
private fun HistoryTabContent(
    items: List<HistoryScheduleItemUiModel>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    HistoryScheduleList(
        items = items,
        state = listState,
        modifier = modifier,
    )
}

@Preview(
    name = "Reservations / Student / Default",
    group = "Screen/MySchedule",
    showBackground = true,
    widthDp = 390,
    heightDp = 756,
)
@Composable
private fun `MyScheduleScreenPreview_Reservations_STUDENT_Default`() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleScreen(
            selectedTab = MyScheduleTab.UPCOMING,
            upcomingItems = myScheduleReservationsPreviewItems(),
            historyItems = myScheduleHistoryPreviewItems(),
        )
    }
}

@Preview(
    name = "History · Student · Default",
    group = "Screen/MySchedule",
    showBackground = true,
    widthDp = 390,
    heightDp = 756,
)
@Composable
private fun MyScheduleScreenPreview_History_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleScreen(
            selectedTab = MyScheduleTab.HISTORY,
            upcomingItems = myScheduleReservationsPreviewItems(),
            historyItems = myScheduleHistoryPreviewItems(),
        )
    }
}
