package com.classitda.feature.student.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.feature.student.StudentBottomTab
import com.classitda.feature.student.StudentTab

@Composable
fun HomeScreen(
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onTabSelected: (StudentTab) -> Unit = {},
) {
    Scaffold(
        containerColor = StuColors.Background,
        topBar = {
            HomeTopBar(
                studioName = "코코필라테스&필라테스 영등점",
                onStudioClick = { /*TODO*/ },
                onNotificationClick = { /*TODO*/ },
            )
        },
        bottomBar = {
            StudentBottomTab(
                selectedTab = StudentTab.HOME,
                onTabSelected = onTabSelected,
            )
        },
    ) { innerPadding ->
        HomeScreenContent(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
@Preview
private fun HomeScreenPreview() {
    AppTheme {
        HomeScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(AppSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionGap),
        ) {
            PendingReservationCard(
                className = "리포머 밸런스",
                instructorName = "이지은",
                classTime = "오늘 오후 7:30",
                remainingMin = 18,
                remainingProgress = 18 / 60f,
                onLaterClick = { /*TODO*/ },
                onApproveClick = { /*TODO*/ },
            )

            UpcomingReservationsSection(
                classDateTime = "8월 6일 · 목요일 오후 7:30",
                className = "리포머 밸런스",
                instructorName = "이지은",
                memo = "실내화를 지참해 주세요.",
                remainingTime = "22시간 남음",
                onDetailClick = { /*TODO*/ },
                onSeeAllClick = { /*TODO*/ },
            )
            MyPassesSection(
                passName = "리포머 20회권",
                expireDateText = "2026.09.30까지",
                totalRemaining = 8,
                reservable = 5,
                cancellable = 2,
                onInfoClick = { /*TODO*/ },
                onPassCardClick = { /*TODO*/ },
                onSeeAllClick = { /*TODO*/ },
            )
            FacilityNoticeSection(
                title = "샤워실 이용 시간이 변경되었어요",
                description = "8월 10일부터 평일 샤워실은 오후 10시까지 운영...",
                date = "2026.08.05",
                onNoticeClick = { /*TODO*/ },
                onSeeAllClick = { /*TODO*/ },
            )
        }
    }
}

@Composable
@Preview
private fun HomeScreenContentPreview() {
    AppTheme {
        HomeScreenContent(
            isRefreshing = false,
            onRefresh = {},
        )
    }
}
