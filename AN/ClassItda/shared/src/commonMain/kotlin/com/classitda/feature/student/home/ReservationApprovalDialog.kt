package com.classitda.feature.student.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors

@Composable
fun ReservationApprovalDialog(
    className: String,
    date: String,
    timeRange: String,
    instructorName: String,
    memo: String,
    passName: String,
    totalRemainingCount: Int,
    reservableCount: Int,
    cancellableCount: Int,
    onCancelClick: () -> Unit,
    onApproveClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        ReservationApprovalDialogContent(
            className = className,
            date = date,
            timeRange = timeRange,
            instructorName = instructorName,
            memo = memo,
            passName = passName,
            totalRemainingCount = totalRemainingCount,
            reservableCount = reservableCount,
            cancellableCount = cancellableCount,
            onCancelClick = onCancelClick,
            onApproveClick = onApproveClick,
        )
    }
}

@Composable
private fun ReservationApprovalDialogContent(
    className: String,
    date: String,
    timeRange: String,
    instructorName: String,
    memo: String,
    passName: String,
    totalRemainingCount: Int,
    reservableCount: Int,
    cancellableCount: Int,
    onCancelClick: () -> Unit,
    onApproveClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(AppShape.Card)
                .background(StuColors.Surface),
    ) {
        Column(modifier = Modifier.padding(horizontal = AppSpacing.xl, vertical = AppSpacing.xxl)) {
            Text(
                text = "예약을 승인하시겠습니까?",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = StuColors.TextPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(AppSpacing.xl))

            Text(
                text = "수업 정보",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                color = StuColors.TextTertiary,
            )

            Spacer(Modifier.height(AppSpacing.sm))

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(StuColors.SurfaceVariant, AppShape.Card)
                        .padding(AppSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                InfoRow(label = "수업", value = className)
                InfoRow(label = "일시", value = date)
                InfoRow(label = "시간", value = timeRange)
                InfoRow(label = "강사", value = "$instructorName 강사")
                InfoRow(label = "메모", value = memo)
            }

            Spacer(Modifier.height(AppSpacing.sectionGap))

            Text(
                text = "사용 수강권",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                color = StuColors.TextTertiary,
            )

            Spacer(Modifier.height(AppSpacing.sm))

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(StuColors.SurfaceVariant, AppShape.Card)
                        .padding(AppSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                Text(
                    text = passName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = StuColors.TextPrimary,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    PassMetricText(
                        label = "전체 잔여",
                        value = "${totalRemainingCount}회",
                        valueColor = StuColors.TextPrimary,
                    )
                    PassMetricText(label = "예약 가능", value = "${reservableCount}회", valueColor = StuColors.Green)
                    PassMetricText(label = "취소 가능", value = "${cancellableCount}회", valueColor = StuColors.TextPrimary)
                }
            }

            Spacer(Modifier.height(AppSpacing.xl))

            Text(
                text = "승인 후 취소 시 잔여 횟수가 차감될 수 있으니\n일정을 다시 한번 확인해 주세요.",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = StuColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "취소",
                modifier =
                    Modifier
                        .weight(1f)
                        .clickable(onClick = onCancelClick)
                        .padding(vertical = AppSpacing.lg),
                style = MaterialTheme.typography.titleMedium,
                color = StuColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "승인하기",
                modifier =
                    Modifier
                        .weight(1f)
                        .clickable(onClick = onApproveClick)
                        .padding(vertical = AppSpacing.lg),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = StuColors.Black,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.width(56.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = StuColors.TextSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = StuColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PassMetricText(
    label: String,
    value: String,
    valueColor: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = StuColors.TextSecondary,
        )
        Text(
            text = value,
            modifier = Modifier.padding(start = AppSpacing.xs),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = valueColor,
        )
    }
}

@Composable
@Preview
private fun ReservationApprovalDialogPreview() {
    AppTheme {
        ReservationApprovalDialogContent(
            className = "리포머 밸런스",
            date = "2026.08.05 (목)",
            timeRange = "오후 7:30 ~ 오후 8:20",
            instructorName = "이지은",
            memo = "준비물 - 수건, 오늘 수업 조금asdgasdgasdgasdgsdga",
            passName = "리포머 20회권",
            totalRemainingCount = 8,
            reservableCount = 5,
            cancellableCount = 2,
            onCancelClick = {},
            onApproveClick = {},
        )
    }
}
