package com.classitda.feature.student.myschedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.student.myschedule.ReservationId
import com.classitda.domain.model.student.myschedule.WaitlistId
import com.classitda.feature.student.myschedule.component.common.MyScheduleTopBar
import com.classitda.feature.student.myschedule.component.list.MyScheduleEmptyContent
import com.classitda.feature.student.myschedule.component.list.MyScheduleInitialErrorContent
import com.classitda.feature.student.myschedule.component.list.MyScheduleLoadingContent
import com.classitda.feature.student.myschedule.component.list.MyScheduleRefreshStatus
import com.classitda.feature.student.myschedule.component.list.MyScheduleTabSelector
import com.classitda.feature.student.myschedule.component.list.UpcomingScheduleSectionList
import com.classitda.feature.student.myschedule.component.list.UsageHistorySectionList
import com.classitda.feature.student.myschedule.contract.MyScheduleAction
import com.classitda.feature.student.myschedule.contract.MyScheduleTab
import com.classitda.feature.student.myschedule.contract.MyScheduleUiState
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleTabState
import com.classitda.feature.student.myschedule.contract.UsageHistoryTabState
import com.classitda.feature.student.myschedule.preview.MyScheduleListStatePreviewFixture
import com.classitda.feature.student.myschedule.preview.MyScheduleUpcomingPreviewFixture
import com.classitda.feature.student.myschedule.preview.MyScheduleUsageHistoryPreviewFixture

@Composable
fun MyScheduleScreen(
    state: MyScheduleUiState,
    onAction: (MyScheduleAction) -> Unit,
    onOpenReservation: (ReservationId) -> Unit,
    onOpenWaitlist: (WaitlistId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val upcomingListState = rememberLazyListState()
    val usageHistoryListState = rememberLazyListState()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background),
    ) {
        MyScheduleTopBar()
        MyScheduleTabSelector(
            selectedTab = state.selectedTab,
            onTabSelected = { tab -> onAction(MyScheduleAction.SelectTab(tab)) },
        )

        when (state.selectedTab) {
            MyScheduleTab.UPCOMING -> {
                UpcomingTabContent(
                    state = state.upcoming,
                    listState = upcomingListState,
                    onAction = onAction,
                    onOpenReservation = onOpenReservation,
                    onOpenWaitlist = onOpenWaitlist,
                    modifier = Modifier.weight(1f),
                )
            }

            MyScheduleTab.HISTORY -> {
                UsageHistoryTabContent(
                    state = state.usageHistory,
                    listState = usageHistoryListState,
                    onAction = onAction,
                    onOpenReservation = onOpenReservation,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun UpcomingTabContent(
    state: UpcomingScheduleTabState,
    listState: LazyListState,
    onAction: (MyScheduleAction) -> Unit,
    onOpenReservation: (ReservationId) -> Unit,
    onOpenWaitlist: (WaitlistId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background),
    ) {
        when (state) {
            UpcomingScheduleTabState.NotLoaded -> {
                // ViewModel 연결 전의 초기 상태는 의도적으로 콘텐츠를 표시하지 않는다.
            }

            UpcomingScheduleTabState.Loading -> {
                MyScheduleLoadingContent()
            }

            UpcomingScheduleTabState.Empty -> {
                MyScheduleEmptyContent(
                    tab = MyScheduleTab.UPCOMING,
                    onAction = {
                        onAction(MyScheduleAction.SelectTab(MyScheduleTab.HISTORY))
                    },
                )
            }

            is UpcomingScheduleTabState.Error -> {
                MyScheduleInitialErrorContent(
                    error = state.error,
                    onRetry = {
                        onAction(MyScheduleAction.Retry(MyScheduleTab.UPCOMING))
                    },
                )
            }

            is UpcomingScheduleTabState.Content -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    MyScheduleRefreshStatus(
                        state = state.refreshState,
                        onRetry = {
                            onAction(MyScheduleAction.Retry(MyScheduleTab.UPCOMING))
                        },
                    )
                    UpcomingScheduleSectionList(
                        sections = state.sections,
                        onOpenReservation = onOpenReservation,
                        onOpenWaitlist = onOpenWaitlist,
                        state = listState,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun UsageHistoryTabContent(
    state: UsageHistoryTabState,
    listState: LazyListState,
    onAction: (MyScheduleAction) -> Unit,
    onOpenReservation: (ReservationId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background),
    ) {
        when (state) {
            UsageHistoryTabState.NotLoaded -> {
                // ViewModel 연결 전의 초기 상태는 의도적으로 콘텐츠를 표시하지 않는다.
            }

            UsageHistoryTabState.Loading -> {
                MyScheduleLoadingContent()
            }

            UsageHistoryTabState.Empty -> {
                MyScheduleEmptyContent(
                    tab = MyScheduleTab.HISTORY,
                    onAction = {
                        onAction(MyScheduleAction.SelectTab(MyScheduleTab.UPCOMING))
                    },
                )
            }

            is UsageHistoryTabState.Error -> {
                MyScheduleInitialErrorContent(
                    error = state.error,
                    onRetry = {
                        onAction(MyScheduleAction.Retry(MyScheduleTab.HISTORY))
                    },
                )
            }

            is UsageHistoryTabState.Content -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    MyScheduleRefreshStatus(
                        state = state.refreshState,
                        onRetry = {
                            onAction(MyScheduleAction.Retry(MyScheduleTab.HISTORY))
                        },
                    )
                    UsageHistorySectionList(
                        sections = state.sections,
                        onOpenReservation = onOpenReservation,
                        state = listState,
                        modifier = Modifier.weight(1f),
                    )
                }
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
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleInteractionHarness(
            initialState = MyScheduleListStatePreviewFixture.content,
        )
    }
}

@Preview(
    name = "F02 Content · Student · Default",
    group = "Screen/MySchedule/Visual",
    showBackground = true,
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MyScheduleScreenPreview_F02Content_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleScreen(
            state = MyScheduleUsageHistoryPreviewFixture.state,
            onAction = {},
            onOpenReservation = {},
            onOpenWaitlist = {},
        )
    }
}

@Preview(
    name = "F02 Interaction · Student",
    group = "Harness/MySchedule",
    showBackground = true,
    widthDp = 390,
    heightDp = 920,
)
@Composable
private fun MyScheduleScreenPreview_F02Interaction_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleInteractionHarness(
            initialState =
                MyScheduleListStatePreviewFixture.content.copy(
                    selectedTab = MyScheduleTab.HISTORY,
                ),
        )
    }
}

@Preview(
    name = "Loading · Student",
    group = "Screen/MySchedule/Async",
    showBackground = true,
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MyScheduleScreenPreview_Loading_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleScreen(
            state = MyScheduleListStatePreviewFixture.upcomingLoading,
            onAction = {},
            onOpenReservation = {},
            onOpenWaitlist = {},
        )
    }
}

@Preview(
    name = "Empty upcoming · Student",
    group = "Screen/MySchedule/Async",
    showBackground = true,
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MyScheduleScreenPreview_EmptyUpcoming_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleScreen(
            state = MyScheduleListStatePreviewFixture.upcomingEmpty,
            onAction = {},
            onOpenReservation = {},
            onOpenWaitlist = {},
        )
    }
}

@Preview(
    name = "Empty history · Student",
    group = "Screen/MySchedule/Async",
    showBackground = true,
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MyScheduleScreenPreview_EmptyHistory_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleScreen(
            state = MyScheduleListStatePreviewFixture.historyEmpty,
            onAction = {},
            onOpenReservation = {},
            onOpenWaitlist = {},
        )
    }
}

@Preview(
    name = "Initial error · Student",
    group = "Screen/MySchedule/Async",
    showBackground = true,
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MyScheduleScreenPreview_InitialError_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleScreen(
            state = MyScheduleListStatePreviewFixture.upcomingError,
            onAction = {},
            onOpenReservation = {},
            onOpenWaitlist = {},
        )
    }
}

@Preview(
    name = "Refreshing content · Student",
    group = "Screen/MySchedule/Async",
    showBackground = true,
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MyScheduleScreenPreview_RefreshingContent_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleScreen(
            state = MyScheduleListStatePreviewFixture.upcomingRefreshing,
            onAction = {},
            onOpenReservation = {},
            onOpenWaitlist = {},
        )
    }
}

@Preview(
    name = "Refresh error · Student",
    group = "Screen/MySchedule/Async",
    showBackground = true,
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MyScheduleScreenPreview_RefreshError_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleScreen(
            state = MyScheduleListStatePreviewFixture.refreshFailed,
            onAction = {},
            onOpenReservation = {},
            onOpenWaitlist = {},
        )
    }
}

@Preview(
    name = "Long content · Student · Large font",
    group = "Screen/MySchedule/Boundary",
    showBackground = true,
    widthDp = 390,
    heightDp = 840,
    fontScale = 1.5f,
)
@Composable
private fun MyScheduleScreenPreview_LongContent_Student_LargeFont() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleScreen(
            state = MyScheduleListStatePreviewFixture.longContent,
            onAction = {},
            onOpenReservation = {},
            onOpenWaitlist = {},
        )
    }
}

@Preview(
    name = "Async actions · Student",
    group = "Harness/MySchedule",
    showBackground = true,
    widthDp = 390,
    heightDp = 920,
)
@Composable
private fun MyScheduleScreenPreview_AsyncActions_Student() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleInteractionHarness(
            initialState = MyScheduleListStatePreviewFixture.asyncInteraction,
        )
    }
}

@Composable
private fun MyScheduleInteractionHarness(initialState: MyScheduleUiState) {
    var state by remember(initialState) { mutableStateOf(initialState) }
    var lastEvent by remember { mutableStateOf("없음") }

    Column {
        Box(modifier = Modifier.height(840.dp)) {
            MyScheduleScreen(
                state = state,
                onAction = { action ->
                    lastEvent = action.toPreviewDescription()
                    when (action) {
                        is MyScheduleAction.SelectTab -> {
                            state = state.copy(selectedTab = action.tab)
                        }

                        is MyScheduleAction.Retry -> {
                            if (
                                action.tab == MyScheduleTab.UPCOMING &&
                                state.upcoming is UpcomingScheduleTabState.Error
                            ) {
                                state = state.copy(upcoming = UpcomingScheduleTabState.Empty)
                            }
                        }
                    }
                },
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
            color = StuColors.SurfaceVariant,
        ) {
            Text(
                text = "마지막 이벤트: $lastEvent",
                modifier = Modifier.padding(AppSpacing.md),
                style = appTypography().bodyMedium,
                color = StuColors.TextSecondary,
            )
        }
    }
}

private fun MyScheduleAction.toPreviewDescription(): String =
    when (this) {
        is MyScheduleAction.SelectTab -> "SelectTab(tab=$tab)"
        is MyScheduleAction.Retry -> "Retry(tab=$tab)"
    }
