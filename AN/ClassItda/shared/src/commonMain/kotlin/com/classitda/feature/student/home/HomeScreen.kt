package com.classitda.feature.student.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.data.repository.home.FakeNoticeRepository
import com.classitda.data.repository.home.FakePassRepository
import com.classitda.data.repository.home.FakeReservationRepository
import com.classitda.feature.student.StudentBottomTab
import com.classitda.feature.student.StudentTab
import com.classitda.feature.student.home.component.PrimaryTextButton
import com.classitda.feature.student.home.model.FacilityNoticeUiModel
import com.classitda.feature.student.home.model.MyPassUiModel
import com.classitda.feature.student.home.model.PendingReservationUiModel
import com.classitda.feature.student.home.model.UpcomingReservationUiModel

@Composable
fun HomeScreen(
    onTabSelected: (StudentTab) -> Unit = {},
    viewModel: HomeViewModel =
        viewModel {
            HomeViewModel(
                reservationRepository = FakeReservationRepository(),
                passRepository = FakePassRepository(),
                noticeRepository = FakeNoticeRepository(),
            )
        },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { message -> snackbarHostState.showSnackbar(message) }
    }

    HomeScreenStateless(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onTabSelected = onTabSelected,
        onRefresh = viewModel::onRefresh,
        onRetry = viewModel::onRetry,
        onPendingReservationApproveClick = viewModel::onPendingReservationApproveClick,
        onApprovalDialogDismiss = viewModel::onApprovalDialogDismiss,
        onApproveReservation = viewModel::onApproveReservation,
        onConfirmedDialogDismiss = viewModel::onConfirmedDialogDismiss,
    )
}

@Composable
private fun HomeScreenStateless(
    uiState: HomeUiState,
    onTabSelected: (StudentTab) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onPendingReservationApproveClick: () -> Unit,
    onApprovalDialogDismiss: () -> Unit,
    onApproveReservation: () -> Unit,
    onConfirmedDialogDismiss: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val content =
        when (uiState) {
            is HomeUiState.Success -> uiState.content
            is HomeUiState.Loading -> uiState.previous
            HomeUiState.InitialLoading, is HomeUiState.Error -> null
        }

    val pendingReservation = content?.pendingReservation
    if (content?.showApprovalDialog == true && pendingReservation != null) {
        ReservationApprovalDialog(
            className = pendingReservation.className,
            date = pendingReservation.dateText,
            timeRange = pendingReservation.timeRangeText,
            instructorName = pendingReservation.instructorName,
            memo = pendingReservation.memo,
            passName = pendingReservation.passName,
            totalRemainingCount = pendingReservation.totalRemainingCount,
            reservableCount = pendingReservation.reservableCount,
            cancellableCount = pendingReservation.cancellableCount,
            onCancelClick = onApprovalDialogDismiss,
            onApproveClick = onApproveReservation,
            onDismissRequest = onApprovalDialogDismiss,
        )
    }

    content?.confirmedReservation?.let { confirmed ->
        ReservationConfirmedDialog(
            className = confirmed.className,
            date = confirmed.dateText,
            timeRange = confirmed.timeRangeText,
            onCheckScheduleClick = {
                onConfirmedDialogDismiss()
                onTabSelected(StudentTab.MY_SCHEDULE)
            },
            onDismissRequest = onConfirmedDialogDismiss,
        )
    }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when {
            uiState is HomeUiState.InitialLoading -> {
                HomeLoadingContent(modifier = Modifier.padding(innerPadding))
            }

            uiState is HomeUiState.Error -> {
                HomeErrorContent(
                    message = uiState.message,
                    onRetryClick = onRetry,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            content != null -> {
                HomeScreenContent(
                    content = content,
                    isRefreshing = uiState is HomeUiState.Loading,
                    onRefresh = onRefresh,
                    onPendingReservationApproveClick = onPendingReservationApproveClick,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    content: HomeContentUiModel,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onPendingReservationApproveClick: () -> Unit,
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
            content.pendingReservation?.let { pending ->
                PendingReservationCard(
                    className = pending.className,
                    instructorName = pending.instructorName,
                    classTime = pending.classTimeText,
                    remainingMin = pending.remainingMin,
                    remainingProgress = pending.remainingProgress,
                    onLaterClick = { /*TODO*/ },
                    onApproveClick = onPendingReservationApproveClick,
                )
            }

            content.upcomingReservation?.let { upcoming ->
                UpcomingReservationsSection(
                    classDateTime = upcoming.classDateTimeText,
                    className = upcoming.className,
                    instructorName = upcoming.instructorName,
                    memo = upcoming.memo,
                    remainingTime = upcoming.remainingTimeText,
                    onDetailClick = { /*TODO*/ },
                    onSeeAllClick = { /*TODO*/ },
                )
            }
            content.myPass?.let { pass ->
                MyPassesSection(
                    passName = pass.passName,
                    expireDateText = pass.expireDateText,
                    totalRemaining = pass.totalRemaining,
                    reservable = pass.reservable,
                    cancellable = pass.cancellable,
                    onInfoClick = { /*TODO*/ },
                    onPassCardClick = { /*TODO*/ },
                    onSeeAllClick = { /*TODO*/ },
                    holdingPeriod = pass.holdingPeriodText,
                )
            }
            content.facilityNotice?.let { notice ->
                FacilityNoticeSection(
                    title = notice.title,
                    description = notice.description,
                    date = notice.dateText,
                    onNoticeClick = { /*TODO*/ },
                    onSeeAllClick = { /*TODO*/ },
                )
            }
        }
    }
}

@Composable
private fun HomeLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().background(StuColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = StuColors.Primary)
    }
}

@Composable
private fun HomeErrorContent(
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
            text = "홈 정보를 불러오지 못했어요",
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
        PrimaryTextButton(
            content = "다시 시도",
            onClick = onRetryClick,
        )
    }
}

private fun sampleHomeContent(): HomeContentUiModel =
    HomeContentUiModel(
        pendingReservation =
            PendingReservationUiModel(
                reservationId = "pending-reservation-1",
                className = "리포머 밸런스",
                instructorName = "이지은",
                classTimeText = "오늘 오후 7:30",
                remainingMin = 18,
                remainingProgress = 18 / 60f,
                dateText = "2026.08.05 (목)",
                timeRangeText = "오후 7:30 ~ 오후 8:20",
                memo = "준비물 - 수건, 오늘 수업 조금...",
                passName = "리포머 20회권",
                totalRemainingCount = 8,
                reservableCount = 5,
                cancellableCount = 2,
            ),
        upcomingReservation =
            UpcomingReservationUiModel(
                classDateTimeText = "8월 6일 · 목요일 오후 7:30",
                className = "리포머 밸런스",
                instructorName = "이지은",
                memo = "실내화를 지참해 주세요.",
                remainingTimeText = "22시간 남음",
            ),
        myPass =
            MyPassUiModel(
                passName = "리포머 20회권",
                expireDateText = "2026.09.30까지",
                totalRemaining = 8,
                reservable = 5,
                cancellable = 2,
            ),
        facilityNotice =
            FacilityNoticeUiModel(
                title = "샤워실 이용 시간이 변경되었어요",
                description = "8월 10일부터 평일 샤워실은 오후 10시까지 운영...",
                dateText = "2026.08.05",
            ),
    )

@Composable
@Preview
private fun HomeScreenContentPreview() {
    AppTheme {
        HomeScreenContent(
            content = sampleHomeContent(),
            isRefreshing = false,
            onRefresh = {},
            onPendingReservationApproveClick = {},
        )
    }
}

@Composable
@Preview
private fun HomeScreenPreview() {
    AppTheme {
        HomeScreenStateless(
            uiState = HomeUiState.Success(sampleHomeContent()),
            onTabSelected = {},
            onRefresh = {},
            onRetry = {},
            onPendingReservationApproveClick = {},
            onApprovalDialogDismiss = {},
            onApproveReservation = {},
            onConfirmedDialogDismiss = {},
        )
    }
}

@Composable
@Preview
private fun HomeScreenLoadingPreview() {
    AppTheme {
        HomeScreenStateless(
            uiState = HomeUiState.InitialLoading,
            onTabSelected = {},
            onRefresh = {},
            onRetry = {},
            onPendingReservationApproveClick = {},
            onApprovalDialogDismiss = {},
            onApproveReservation = {},
            onConfirmedDialogDismiss = {},
        )
    }
}

@Composable
@Preview
private fun HomeScreenErrorPreview() {
    AppTheme {
        HomeScreenStateless(
            uiState = HomeUiState.Error("네트워크 연결을 확인해주세요."),
            onTabSelected = {},
            onRefresh = {},
            onRetry = {},
            onPendingReservationApproveClick = {},
            onApprovalDialogDismiss = {},
            onApproveReservation = {},
            onConfirmedDialogDismiss = {},
        )
    }
}
