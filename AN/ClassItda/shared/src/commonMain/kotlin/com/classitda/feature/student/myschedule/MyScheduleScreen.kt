package com.classitda.feature.student.myschedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.domain.model.student.myschedule.ReservationId
import com.classitda.domain.model.student.myschedule.WaitlistId
import com.classitda.feature.student.myschedule.component.common.MyScheduleTopBar
import com.classitda.feature.student.myschedule.component.list.MyScheduleTabSelector
import com.classitda.feature.student.myschedule.component.list.UpcomingScheduleSectionList
import com.classitda.feature.student.myschedule.contract.MyScheduleAction
import com.classitda.feature.student.myschedule.contract.MyScheduleTab
import com.classitda.feature.student.myschedule.contract.MyScheduleUiState
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleTabState
import com.classitda.feature.student.myschedule.preview.MyScheduleUpcomingPreviewFixture

@Composable
fun MyScheduleScreen(
    state: MyScheduleUiState,
    onAction: (MyScheduleAction) -> Unit,
    onOpenReservation: (ReservationId) -> Unit,
    onOpenWaitlist: (WaitlistId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val upcomingListState = rememberLazyListState()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        MyScheduleTopBar()
        MyScheduleTabSelector(
            selectedTab = state.selectedTab,
            onTabSelected = { tab -> onAction(MyScheduleAction.SelectTab(tab)) },
        )

        when (state.selectedTab) {
            MyScheduleTab.UPCOMING -> {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.background),
                ) {
                    when (val upcoming = state.upcoming) {
                        is UpcomingScheduleTabState.Content -> {
                            UpcomingScheduleSectionList(
                                sections = upcoming.sections,
                                onOpenReservation = onOpenReservation,
                                onOpenWaitlist = onOpenWaitlist,
                                state = upcomingListState,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        UpcomingScheduleTabState.NotLoaded,
                        UpcomingScheduleTabState.Loading,
                        UpcomingScheduleTabState.Empty,
                        is UpcomingScheduleTabState.Error,
                        -> {}
                    }
                }
            }

            MyScheduleTab.HISTORY -> {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.background),
                )
            }
        }
    }
}

@Preview(
    name = "F01 Content · Student · Default",
    group = "Screen/MySchedule/Visual",
    showBackground = true,
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MyScheduleScreenPreview_F01Content_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleScreen(
            state = MyScheduleUpcomingPreviewFixture.state,
            onAction = {},
            onOpenReservation = {},
            onOpenWaitlist = {},
        )
    }
}

@Preview(
    name = "F01 Interaction · Student",
    group = "Harness/MySchedule",
    showBackground = true,
    widthDp = 390,
    heightDp = 920,
)
@Composable
private fun MyScheduleScreenPreview_F01Interaction_Student() {
    var lastEvent by remember { mutableStateOf("없음") }

    AppTheme(theme = ThemeType.STUDENT) {
        Column {
            Box(modifier = Modifier.height(840.dp)) {
                MyScheduleScreen(
                    state = MyScheduleUpcomingPreviewFixture.state,
                    onAction = { action -> lastEvent = action.toPreviewDescription() },
                    onOpenReservation = { id ->
                        lastEvent = "OpenReservation(id=${id.value})"
                    },
                    onOpenWaitlist = { id ->
                        lastEvent = "OpenWaitlist(id=${id.value})"
                    },
                )
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = "마지막 이벤트: $lastEvent",
                    modifier = Modifier.padding(AppSpacing.md),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun MyScheduleAction.toPreviewDescription(): String =
    when (this) {
        is MyScheduleAction.SelectTab -> "SelectTab(tab=$tab)"
        is MyScheduleAction.Retry -> "Retry(tab=$tab)"
    }
