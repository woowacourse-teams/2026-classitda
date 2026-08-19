package com.classitda.feature.student.mypage.mypass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.core.designsystem.component.NavigateBackTopBar
import com.classitda.core.designsystem.component.PrimaryButton
import com.classitda.feature.student.mypage.mypass.model.MyPassCardUiModel
import com.classitda.feature.student.mypage.mypass.model.MyPassStatus
import com.classitda.feature.student.mypage.mypass.model.MyPassTab
import com.classitda.feature.student.mypage.mypass.model.MyPassTabState
import com.classitda.feature.student.mypage.mypass.model.MyPassUiState

@Composable
fun MyPassScreen(
    uiState: MyPassUiState,
    onTabSelected: (MyPassTab) -> Unit,
    onNavigateBack: () -> Unit,
    onRetry: (MyPassTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabState =
        when (uiState.selectedTab) {
            MyPassTab.IN_USE -> uiState.inUse
            MyPassTab.EXPIRED -> uiState.expired
        }

    Column(
        modifier = modifier.fillMaxSize().background(StuColors.Background),
    ) {
        NavigateBackTopBar(
            onNavigateBack = onNavigateBack,
            modifier = Modifier.background(StuColors.White),
            title = "내 수강권",
        )
        MyPassTabRow(
            selectedTab = uiState.selectedTab,
            onTabSelected = onTabSelected,
        )
        MyPassTabContent(
            tabState = tabState,
            onRetryClick = { onRetry(uiState.selectedTab) },
            onRefresh = { onRetry(uiState.selectedTab) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MyPassTabContent(
    tabState: MyPassTabState,
    onRetryClick: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (tabState) {
            MyPassTabState.Initial -> {
                // 다른 탭이 아직 로드되지 않은 초기 상태는 의도적으로 콘텐츠를 표시하지 않는다.
            }

            is MyPassTabState.Loading -> {
                val previousPasses = tabState.previousPasses
                if (previousPasses != null) {
                    MyPassRefreshableList(
                        passes = previousPasses,
                        isRefreshing = true,
                        onRefresh = onRefresh,
                    )
                } else {
                    MyPassLoadingContent()
                }
            }

            is MyPassTabState.Content -> {
                MyPassRefreshableList(
                    passes = tabState.passes,
                    isRefreshing = false,
                    onRefresh = onRefresh,
                )
            }

            is MyPassTabState.Error -> {
                MyPassErrorContent(
                    message = tabState.message,
                    onRetryClick = onRetryClick,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyPassRefreshableList(
    passes: List<MyPassCardUiModel>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        MyPassList(passes = passes)
    }
}

@Composable
private fun MyPassList(
    passes: List<MyPassCardUiModel>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
        items(passes, key = { it.id }) { item ->
            MyPassCard(item = item)
        }
    }
}

@Composable
private fun MyPassLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().background(StuColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = StuColors.Primary)
    }
}

@Composable
private fun MyPassErrorContent(
    message: String?,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background)
                .padding(AppSpacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "수강권 정보를 불러오지 못했어요",
            style = MaterialTheme.typography.titleMedium,
            color = StuColors.TextPrimary,
        )
        if (message != null) {
            Spacer(Modifier.height(AppSpacing.sm))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = StuColors.TextSecondary,
            )
        }
        Spacer(Modifier.height(AppSpacing.sectionGap))
        PrimaryButton(
            text = "다시 시도",
            onClick = onRetryClick,
        )
    }
}

@Composable
private fun MyPassTabRow(
    selectedTab: MyPassTab,
    onTabSelected: (MyPassTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = appTypography()
    val tabs = listOf(MyPassTab.IN_USE, MyPassTab.EXPIRED)

    Row(
        modifier = modifier.fillMaxWidth().background(StuColors.Surface),
    ) {
        tabs.forEach { tab ->
            val isSelected = selectedTab == tab
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .selectable(
                            selected = isSelected,
                            onClick = { onTabSelected(tab) },
                            role = Role.Tab,
                        ),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.lg),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text =
                            when (tab) {
                                MyPassTab.IN_USE -> "사용 중"
                                MyPassTab.EXPIRED -> "만료/종료"
                            },
                        style =
                            typography.titleMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            ),
                        color = if (isSelected) StuColors.TextPrimary else StuColors.TextSecondary,
                    )
                }
                HorizontalDivider(
                    color = if (isSelected) StuColors.TextPrimary else StuColors.Divider,
                )
            }
        }
    }
}

private val previewInUsePasses =
    listOf(
        MyPassCardUiModel(
            id = "pass-reformer-20",
            status = MyPassStatus.IN_USE,
            periodLabel = "기간 무제한",
            title = "리포머 20회권",
            totalRemainingCount = 8,
            reservableCount = 5,
            cancellableCount = 2,
        ),
        MyPassCardUiModel(
            id = "pass-pilates-1month",
            status = MyPassStatus.IN_USE,
            periodLabel = "2026.07.21 ~ 2026.08.20",
            title = "1개월 필라테스 수강권",
            totalRemainingCount = 8,
            reservableCount = 5,
            cancellableCount = 2,
        ),
    )

private val previewExpiredPasses =
    listOf(
        MyPassCardUiModel(
            id = "pass-expired-gigu",
            status = MyPassStatus.EXPIRED,
            periodLabel = "2025.09.30 ~ 2025.12.30",
            title = "기구 필라테스 10회권",
            totalRemainingCount = 8,
            reservableCount = 0,
            cancellableCount = 0,
        ),
        MyPassCardUiModel(
            id = "pass-terminated-open-event",
            status = MyPassStatus.TERMINATED,
            periodLabel = "2025.05.01 ~ 2025.08.15",
            title = "오픈 기념 이벤트 20회권",
            totalRemainingCount = 0,
            reservableCount = 0,
            cancellableCount = 0,
        ),
    )

private val previewTabs = listOf(MyPassTab.IN_USE, MyPassTab.EXPIRED)

@Preview
@Composable
private fun MyPassScreenPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xxxl)) {
            previewTabs.forEach { tab ->
                Box(modifier = Modifier.height(840.dp)) {
                    MyPassScreen(
                        uiState =
                            MyPassUiState(
                                selectedTab = tab,
                                inUse = MyPassTabState.Content(previewInUsePasses),
                                expired = MyPassTabState.Content(previewExpiredPasses),
                            ),
                        onTabSelected = {},
                        onNavigateBack = {},
                        onRetry = {},
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MyPassScreenLoadingPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        Box(modifier = Modifier.height(840.dp)) {
            MyPassScreen(
                uiState = MyPassUiState(inUse = MyPassTabState.Loading()),
                onTabSelected = {},
                onNavigateBack = {},
                onRetry = {},
            )
        }
    }
}

@Preview
@Composable
private fun MyPassScreenRefreshingPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        Box(modifier = Modifier.height(840.dp)) {
            MyPassScreen(
                uiState = MyPassUiState(inUse = MyPassTabState.Loading(previousPasses = previewInUsePasses)),
                onTabSelected = {},
                onNavigateBack = {},
                onRetry = {},
            )
        }
    }
}

@Preview
@Composable
private fun MyPassScreenErrorPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        Box(modifier = Modifier.height(840.dp)) {
            MyPassScreen(
                uiState = MyPassUiState(inUse = MyPassTabState.Error("네트워크 연결을 확인해주세요.")),
                onTabSelected = {},
                onNavigateBack = {},
                onRetry = {},
            )
        }
    }
}
