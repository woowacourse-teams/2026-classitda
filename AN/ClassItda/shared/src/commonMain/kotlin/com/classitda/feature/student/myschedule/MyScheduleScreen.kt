package com.classitda.feature.student.myschedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.student.myschedule.component.HistoryScheduleList
import com.classitda.feature.student.myschedule.component.MyScheduleTabRow
import com.classitda.feature.student.myschedule.component.MyScheduleTopBar
import com.classitda.feature.student.myschedule.component.UpcomingScheduleList
import com.classitda.feature.student.myschedule.contract.HistoryScheduleItemUiModel
import com.classitda.feature.student.myschedule.contract.MyScheduleAction
import com.classitda.feature.student.myschedule.contract.MyScheduleContentState
import com.classitda.feature.student.myschedule.contract.MyScheduleTab
import com.classitda.feature.student.myschedule.contract.MyScheduleUiState
import com.classitda.feature.student.myschedule.contract.ScheduleItemId
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleItemUiModel
import com.classitda.feature.student.myschedule.preview.myScheduleHistoryPreviewItems
import com.classitda.feature.student.myschedule.preview.myScheduleReservationsPreviewItems

@Composable
fun MyScheduleScreen(
    state: MyScheduleUiState,
    onAction: (MyScheduleAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheduleList = state as? MyScheduleUiState.ScheduleList ?: return
    val upcomingListState = rememberLazyListState()
    val historyListState = rememberLazyListState()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        MyScheduleTopBar()
        MyScheduleTabRow(
            selectedTab = scheduleList.selectedTab,
            onTabSelected = { onAction(MyScheduleAction.SelectTab(it)) },
        )

        when (scheduleList.selectedTab) {
            MyScheduleTab.UPCOMING -> {
                UpcomingTabContent(
                    content = scheduleList.upcomingContent,
                    listState = upcomingListState,
                    onItemClick = { onAction(MyScheduleAction.OpenScheduleDetail(it)) },
                    modifier = Modifier.weight(1f),
                )
            }

            MyScheduleTab.HISTORY -> {
                HistoryTabContent(
                    content = scheduleList.historyContent,
                    listState = historyListState,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun UpcomingTabContent(
    content: MyScheduleContentState<UpcomingScheduleItemUiModel>,
    listState: LazyListState,
    onItemClick: (ScheduleItemId) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (content) {
        is MyScheduleContentState.Content -> {
            UpcomingScheduleList(
                items = content.items,
                onItemClick = onItemClick,
                state = listState,
                modifier = modifier,
            )
        }

        MyScheduleContentState.Empty,
        MyScheduleContentState.InitialFailure,
        MyScheduleContentState.InitialLoading,
        -> {}
    }
}

@Composable
private fun HistoryTabContent(
    content: MyScheduleContentState<HistoryScheduleItemUiModel>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    when (content) {
        is MyScheduleContentState.Content -> {
            HistoryScheduleList(
                items = content.items,
                state = listState,
                modifier = modifier,
            )
        }

        MyScheduleContentState.Empty,
        MyScheduleContentState.InitialFailure,
        MyScheduleContentState.InitialLoading,
        -> {}
    }
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
            state =
                MyScheduleUiState.ScheduleList(
                    selectedTab = MyScheduleTab.UPCOMING,
                    upcomingContent = MyScheduleContentState.Content(myScheduleReservationsPreviewItems()),
                    historyContent = MyScheduleContentState.Content(myScheduleHistoryPreviewItems()),
                ),
            onAction = {},
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
            state =
                MyScheduleUiState.ScheduleList(
                    selectedTab = MyScheduleTab.HISTORY,
                    upcomingContent = MyScheduleContentState.Content(myScheduleReservationsPreviewItems()),
                    historyContent = MyScheduleContentState.Content(myScheduleHistoryPreviewItems()),
                ),
            onAction = {},
        )
    }
}

@Preview(
    name = "Tabs / Interactive / Student",
    group = "Screen/MySchedule",
    showBackground = true,
    widthDp = 390,
    heightDp = 756,
)
@Composable
private fun MyScheduleScreenPreview_Tabs_Student_Interactive() {
    AppTheme(theme = ThemeType.STUDENT) {
        var selectedTab by remember { mutableStateOf(MyScheduleTab.UPCOMING) }
        val upcomingItems = myScheduleReservationsPreviewItems()
        val historyItems = myScheduleHistoryPreviewItems()

        MyScheduleScreen(
            state =
                MyScheduleUiState.ScheduleList(
                    selectedTab = selectedTab,
                    upcomingContent = MyScheduleContentState.Content(upcomingItems),
                    historyContent = MyScheduleContentState.Content(historyItems),
                ),
            onAction = { action ->
                if (action is MyScheduleAction.SelectTab) {
                    selectedTab = action.tab
                }
            },
        )
    }
}
