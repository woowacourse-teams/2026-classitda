package com.classitda.feature.student.mypage.mypassdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_expand_more
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.component.NavigateBackTopBar
import com.classitda.core.designsystem.component.PrimaryButton
import com.classitda.feature.student.mypage.mypass.model.MyPassStatus
import com.classitda.feature.student.mypage.mypassdetail.model.MyPassDetailUiModel
import com.classitda.feature.student.mypage.mypassdetail.model.MyPassDetailUiState
import com.classitda.feature.student.mypage.mypassdetail.model.MyPassHistoryChangeType
import com.classitda.feature.student.mypage.mypassdetail.model.MyPassHistoryEntryUiModel
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource

@Composable
fun MyPassDetailScreen(
    uiState: MyPassDetailUiState,
    onNavigateBack: () -> Unit,
    onHoldRequestClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().background(StuColors.Background),
    ) {
        NavigateBackTopBar(
            onNavigateBack = onNavigateBack,
            modifier = Modifier.background(StuColors.White),
            title = "내 수강권 상세보기",
        )
        when (uiState) {
            MyPassDetailUiState.Loading -> {
                MyPassDetailLoadingContent(modifier = Modifier.weight(1f))
            }

            is MyPassDetailUiState.Error -> {
                MyPassDetailErrorContent(
                    message = uiState.message,
                    onRetryClick = onRetry,
                    modifier = Modifier.weight(1f),
                )
            }

            is MyPassDetailUiState.Content -> {
                MyPassDetailContent(
                    uiModel = uiState.detail,
                    onHoldRequestClick = onHoldRequestClick,
                    modifier = Modifier.weight(1f),
                )
            }

            MyPassDetailUiState.NotFound -> {
                MyPassDetailNotFoundContent(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MyPassDetailContent(
    uiModel: MyPassDetailUiModel,
    onHoldRequestClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xxl),
    ) {
        MyPassDetailSummaryCard(
            uiModel = uiModel,
            onHoldRequestClick = onHoldRequestClick,
        )
        MyPassDetailInfoCard(uiModel = uiModel)
        MyPassHistorySection(entries = uiModel.historyEntries)
    }
}

@Composable
private fun MyPassDetailLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().background(StuColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = StuColors.Primary)
    }
}

@Composable
private fun MyPassDetailErrorContent(
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
private fun MyPassDetailNotFoundContent(modifier: Modifier = Modifier) {
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
            text = "수강권을 찾을 수 없어요",
            style = MaterialTheme.typography.titleMedium,
            color = StuColors.TextPrimary,
        )
        Spacer(Modifier.height(AppSpacing.sm))
        Text(
            text = "삭제되었거나 잘못된 수강권이에요.",
            style = MaterialTheme.typography.bodyMedium,
            color = StuColors.TextSecondary,
        )
    }
}

@Composable
private fun MyPassDetailSummaryCard(
    uiModel: MyPassDetailUiModel,
    onHoldRequestClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(AppShape.Card)
                .background(StuColors.Surface)
                .padding(AppSpacing.cardPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MyPassDetailStatusBadge(status = uiModel.status)
            Text(
                text = uiModel.dDayLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = StuColors.TextSecondary,
            )
        }
        Spacer(Modifier.height(AppSpacing.sm))

        Text(
            text = uiModel.title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
        )
        Spacer(Modifier.height(AppSpacing.xs))
        Text(
            text = uiModel.studioName,
            style = MaterialTheme.typography.bodyMedium,
            color = StuColors.TextSecondary,
        )

        Spacer(Modifier.height(AppSpacing.sectionGap))
        MyPassDetailMetricRow(
            totalRemainingCount = uiModel.totalRemainingCount,
            reservableCount = uiModel.reservableCount,
            cancellableCount = uiModel.cancellableCount,
        )

        Spacer(Modifier.height(AppSpacing.xxl))
        PrimaryButton(
            text = "홀딩 신청",
            onClick = onHoldRequestClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun MyPassDetailStatusBadge(
    status: MyPassStatus,
    modifier: Modifier = Modifier,
) {
    val label =
        when (status) {
            MyPassStatus.IN_USE -> "사용 중"
            MyPassStatus.EXPIRED -> "만료"
            MyPassStatus.TERMINATED -> "종료"
        }

    Box(
        modifier =
            modifier
                .clip(AppShape.Pill)
                .background(StuColors.SurfaceVariant)
                .padding(
                    horizontal = AppSpacing.chipHorizontalPadding,
                    vertical = AppSpacing.chipVerticalPadding,
                ),
    ) {
        Text(
            text = label,
            color = StuColors.TextSecondary,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun MyPassDetailMetricRow(
    totalRemainingCount: Int,
    reservableCount: Int,
    cancellableCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MyPassDetailMetric(
            label = "전체 잔여",
            value = "${totalRemainingCount}회",
            modifier = Modifier.weight(1f),
        )
        VerticalDivider(
            modifier = Modifier.fillMaxHeight().width(1.dp),
            color = StuColors.Divider,
        )
        MyPassDetailMetric(
            label = "예약 가능",
            value = "${reservableCount}회",
            modifier = Modifier.weight(1f),
        )
        VerticalDivider(
            modifier = Modifier.fillMaxHeight().width(1.dp),
            color = StuColors.Divider,
        )
        MyPassDetailMetric(
            label = "취소 가능",
            value = "${cancellableCount}회",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MyPassDetailMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = StuColors.TextSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
        )
    }
}

@Composable
private fun MyPassDetailInfoCard(
    uiModel: MyPassDetailUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(AppShape.Card)
                .background(StuColors.Surface)
                .padding(AppSpacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        MyPassDetailInfoRow(label = "유효 기간", value = uiModel.validPeriodLabel)
        HorizontalDivider(color = StuColors.Divider)
        MyPassDetailInfoRow(label = "홀딩 가능 일수", value = uiModel.holdingDaysLabel)
    }
}

@Composable
private fun MyPassDetailInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = StuColors.TextSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
        )
    }
}

@Composable
private fun MyPassHistorySection(
    entries: List<MyPassHistoryEntryUiModel>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "이력 확인",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = StuColors.TextPrimary,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "최신순",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StuColors.TextSecondary,
                )
                Icon(
                    painter = painterResource(Res.drawable.ic_expand_more),
                    contentDescription = null,
                    tint = StuColors.TextSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(Modifier.height(AppSpacing.lg))
        Column {
            entries.forEachIndexed { index, entry ->
                MyPassHistoryRow(
                    entry = entry,
                    showConnector = index != entries.lastIndex,
                )
            }
        }
    }
}

@Composable
private fun MyPassHistoryRow(
    entry: MyPassHistoryEntryUiModel,
    showConnector: Boolean,
    modifier: Modifier = Modifier,
) {
    val valueColor =
        when (entry.changeType) {
            MyPassHistoryChangeType.DEDUCTION -> StuColors.Red
            else -> StuColors.TextPrimary
        }

    Row(modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(
            modifier = Modifier.width(20.dp).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .padding(top = 4.dp)
                        .size(8.dp)
                        .border(1.5.dp, StuColors.DividerStrong, CircleShape),
            )
            if (showConnector) {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(top = AppSpacing.xs)
                            .width(1.dp)
                            .background(StuColors.Divider),
                )
            }
        }
        Spacer(Modifier.width(AppSpacing.md))
        Column(
            modifier = Modifier.weight(1f).padding(bottom = AppSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = StuColors.TextPrimary,
                )
                Text(
                    text = entry.changeLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = valueColor,
                )
            }
            Text(
                text = entry.description,
                style = MaterialTheme.typography.bodySmall,
                color = StuColors.TextSecondary,
            )
            Text(
                text = entry.dateLabel,
                style = MaterialTheme.typography.bodySmall,
                color = StuColors.TextTertiary,
            )
        }
    }
}

private val previewHistoryEntries =
    listOf(
        MyPassHistoryEntryUiModel(
            id = "history-1",
            title = "수업 예약 차감",
            changeLabel = "-1회",
            changeType = MyPassHistoryChangeType.DEDUCTION,
            description = "리포머 밸런스 (이지은 강사)",
            dateLabel = "2026.08.05 14:20",
        ),
        MyPassHistoryEntryUiModel(
            id = "history-2",
            title = "취소 복구",
            changeLabel = "+1회",
            changeType = MyPassHistoryChangeType.RESTORATION,
            description = "체어 베이직 (박소연 강사)",
            dateLabel = "2026.08.03 09:12",
        ),
        MyPassHistoryEntryUiModel(
            id = "history-3",
            title = "홀딩 적용",
            changeLabel = "7일 중지",
            changeType = MyPassHistoryChangeType.HOLD,
            description = "개인 사정으로 인한 일시 정지",
            dateLabel = "2026.07.15 ~ 2026.07.21",
        ),
        MyPassHistoryEntryUiModel(
            id = "history-4",
            title = "수강권 발급",
            changeLabel = "+20회",
            changeType = MyPassHistoryChangeType.ISSUANCE,
            description = "리포머 20회권 결제 완료",
            dateLabel = "2026.03.31 11:00",
        ),
    )

private val previewUiModel =
    MyPassDetailUiModel(
        status = MyPassStatus.IN_USE,
        title = "리포머 20회권",
        studioName = "밸런스 필라테스 성수점",
        dDayLabel = "D-56",
        totalRemainingCount = 8,
        reservableCount = 5,
        cancellableCount = 2,
        validPeriodLabel = "2026.03.31 ~ 2026.09.30",
        holdingDaysLabel = "총 30일 (잔여 15일)",
        currentExpireDate = LocalDate(2026, 9, 30),
        historyEntries = previewHistoryEntries,
    )

@Preview
@Composable
private fun MyPassDetailScreenPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyPassDetailScreen(
            uiState = MyPassDetailUiState.Content(previewUiModel),
            onNavigateBack = {},
            onHoldRequestClick = {},
            onRetry = {},
        )
    }
}

@Preview
@Composable
private fun MyPassDetailScreenLoadingPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyPassDetailScreen(
            uiState = MyPassDetailUiState.Loading,
            onNavigateBack = {},
            onHoldRequestClick = {},
            onRetry = {},
        )
    }
}

@Preview
@Composable
private fun MyPassDetailScreenErrorPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyPassDetailScreen(
            uiState = MyPassDetailUiState.Error("네트워크 연결을 확인해주세요."),
            onNavigateBack = {},
            onHoldRequestClick = {},
            onRetry = {},
        )
    }
}

@Preview
@Composable
private fun MyPassDetailScreenNotFoundPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyPassDetailScreen(
            uiState = MyPassDetailUiState.NotFound,
            onNavigateBack = {},
            onHoldRequestClick = {},
            onRetry = {},
        )
    }
}
