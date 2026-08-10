package com.classitda.feature.student.myschedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.student.myschedule.component.MyScheduleTabRow
import com.classitda.feature.student.myschedule.component.MyScheduleTopBar
import com.classitda.feature.student.myschedule.component.UpcomingScheduleList
import com.classitda.feature.student.myschedule.contract.ActiveScheduleItemUiModel
import com.classitda.feature.student.myschedule.contract.MyScheduleAction
import com.classitda.feature.student.myschedule.contract.MyScheduleContentState
import com.classitda.feature.student.myschedule.contract.MyScheduleTab
import com.classitda.feature.student.myschedule.contract.MyScheduleUiState
import com.classitda.feature.student.myschedule.preview.myScheduleReservationsPreviewItems

@Composable
fun MyScheduleScreen(
    state: MyScheduleUiState,
    onAction: (MyScheduleAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheduleList = state as? MyScheduleUiState.ScheduleList ?: return

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

        val content = scheduleList.content
        if (scheduleList.selectedTab == MyScheduleTab.UPCOMING && content is MyScheduleContentState.Content) {
            val upcomingItems = content.items.filterIsInstance<ActiveScheduleItemUiModel>()
            if (upcomingItems.isNotEmpty()) {
                UpcomingScheduleList(
                    items = upcomingItems,
                    onItemClick = { onAction(MyScheduleAction.OpenScheduleDetail(it)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
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
                    content = MyScheduleContentState.Content(myScheduleReservationsPreviewItems),
                ),
            onAction = {},
        )
    }
}
