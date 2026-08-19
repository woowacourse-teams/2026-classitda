package com.classitda.feature.student.mypage.mypass

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.student.mypage.mypass.model.MyPassCardUiModel
import com.classitda.feature.student.mypage.mypass.model.MyPassStatus

@Composable
fun MyPassCard(
    item: MyPassCardUiModel,
    modifier: Modifier = Modifier,
) {
    val emphasisColor = if (item.status == MyPassStatus.IN_USE) StuColors.TextPrimary else StuColors.TextSecondary

    Column(
        modifier =
            modifier
                .clip(shape = AppShape.Card)
                .clickable(onClick = { /* TODO*/ })
                .background(StuColors.Surface)
                .padding(AppSpacing.cardPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MyPassStatusBadge(status = item.status)

            Text(
                text = item.periodLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = StuColors.TextSecondary,
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.sm))

        Text(
            text = item.title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = emphasisColor,
        )

        item.holdingPeriod?.let { holdingPeriod ->
            Spacer(modifier = Modifier.height(AppSpacing.md))

            Text(
                text = "홀딩 기간: $holdingPeriod",
                style = MaterialTheme.typography.bodySmall,
                color = StuColors.Red,
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.sectionGap))
        MyPassMetricRow(
            totalRemainingCount = item.totalRemainingCount,
            reservableCount = item.reservableCount,
            cancellableCount = item.cancellableCount,
            valueColor = emphasisColor,
        )
    }
}

@Composable
private fun MyPassMetricRow(
    totalRemainingCount: Int,
    reservableCount: Int,
    cancellableCount: Int,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MyPassMetric(
            label = "전체 잔여",
            count = totalRemainingCount,
            valueColor = valueColor,
            modifier = Modifier.weight(1f),
        )
        VerticalDivider(
            modifier = Modifier.fillMaxHeight().width(1.dp),
            color = StuColors.Divider,
        )
        MyPassMetric(
            label = "예약 가능",
            count = reservableCount,
            valueColor = valueColor,
            modifier = Modifier.weight(1f),
        )
        VerticalDivider(
            modifier = Modifier.fillMaxHeight().width(1.dp),
            color = StuColors.Divider,
        )
        MyPassMetric(
            label = "취소 가능",
            count = cancellableCount,
            valueColor = valueColor,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MyPassMetric(
    label: String,
    count: Int,
    valueColor: Color,
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
            text = "${count}회",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = valueColor,
        )
    }
}

@Composable
private fun MyPassStatusBadge(
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

private val previewItems =
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
            holdingPeriod = "2026.19.29 ~ 2026.19.34",
        ),
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

@Preview
@Composable
private fun MyPassCardPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        Column(
            modifier = Modifier.padding(AppSpacing.screenPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            previewItems.forEach { item ->
                MyPassCard(item = item)
            }
        }
    }
}
