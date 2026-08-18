package com.classitda.feature.student.reservation.complete

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_check
import classitda.shared.generated.resources.ic_close
import classitda.shared.generated.resources.ic_info
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.component.PrimaryButton
import org.jetbrains.compose.resources.painterResource

internal data class WaitlistCompleteUiModel(
    val id: String,
    val className: String,
    val dateText: String,
    val timeText: String,
    val instructorName: String,
    val roomName: String,
    val classPassName: String,
    val remainingCountText: String,
)

@Composable
internal fun WaitlistCompleteScreen(
    reservation: WaitlistCompleteUiModel,
    onCloseClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = StuColors.Background,
        topBar = { WaitlistCompleteTopBar(onCloseClick = onCloseClick) },
        bottomBar = {
            WaitlistCompleteBottomBar(
                onScheduleClick = onScheduleClick,
                onHomeClick = onHomeClick,
            )
        },
    ) { contentPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(horizontal = AppSpacing.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            Column(
                modifier = Modifier.padding(vertical = AppSpacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(56.dp)
                            .background(StuColors.PrimaryColor, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_check),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = StuColors.White,
                    )
                }
                Text(
                    text = "수업 대기 완료!",
                    color = StuColors.TextPrimary,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = "${reservation.instructorName}님의 수업 대기가\n성공적으로 완료되었습니다.",
                    color = StuColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }

            WaitlistCompleteSummary(reservation = reservation)

            WaitlistNoticeCard(
                modifier = Modifier.padding(vertical = AppSpacing.sectionGap),
            )
        }
    }
}

@Composable
private fun WaitlistCompleteTopBar(
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(StuColors.White)
                .padding(horizontal = AppSpacing.screenPadding),
    ) {
        Text(
            text = "대기 예약 완료",
            color = StuColors.TextPrimary,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Center),
        )
        Box(
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onCloseClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = "닫기",
                modifier = Modifier.size(18.dp),
                tint = StuColors.TextPrimary,
            )
        }
    }
}

@Composable
private fun WaitlistCompleteSummary(
    reservation: WaitlistCompleteUiModel,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShape.Card,
        colors = CardDefaults.cardColors(containerColor = StuColors.White),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = reservation.className,
                    color = StuColors.TextPrimary,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = reservation.dateText,
                    color = StuColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                text = reservation.timeText,
                color = StuColors.TextPrimary,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )

            HorizontalDivider(
                color = StuColors.Divider,
                modifier = Modifier.padding(vertical = AppSpacing.cardItemVerticalGap),
            )

            WaitlistSummaryRow(label = "강사", value = reservation.instructorName)
            WaitlistSummaryRow(label = "사용 수강권", value = reservation.classPassName)
            WaitlistSummaryRow(label = "잔여 예약 가능", value = reservation.remainingCountText)
        }
    }
}

@Composable
private fun WaitlistSummaryRow(
    label: String,
    value: String,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = StuColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            color = StuColors.TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun WaitlistNoticeCard(modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(StuColors.SurfaceVariant, AppShape.Card)
                .padding(AppSpacing.cardPadding),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_info),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = StuColors.TextSecondary,
        )
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(
                text = "대기 확인 안내",
                color = StuColors.TextPrimary,
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = "대기 내역은 내 수업에서 언제든지 확인 및 취소가 가능합니다.",
                color = StuColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun WaitlistCompleteBottomBar(
    onScheduleClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = StuColors.White,
    ) {
        Column(
            modifier =
                Modifier.padding(
                    horizontal = AppSpacing.screenPadding,
                    vertical = AppSpacing.md,
                ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            PrimaryButton(
                text = "내 수업 일정 확인하기",
                onClick = onScheduleClick,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedButton(
                onClick = onHomeClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                shape = AppShape.Card,
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = StuColors.TextSecondary,
                    ),
            ) {
                Text(
                    text = "홈으로 이동",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Preview(name = "대기 예약 완료 화면")
@Composable
private fun WaitlistCompleteScreenPreview() {
    AppTheme {
        WaitlistCompleteScreen(
            reservation =
                WaitlistCompleteUiModel(
                    id = "2",
                    className = "리포머 베이직",
                    dateText = "2026.08.08 (토)",
                    timeText = "오전 10:00 - 10:50",
                    instructorName = "이지은 강사",
                    roomName = "리포머룸",
                    classPassName = "[그룹] 8:1 리포머/체어 10회권",
                    remainingCountText = "2회",
                ),
            onCloseClick = {},
            onScheduleClick = {},
            onHomeClick = {},
        )
    }
}
