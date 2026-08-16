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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.component.PrimaryButton

internal data class ReservationCompleteUiModel(
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
internal fun ReservationCompleteScreen(
    reservation: ReservationCompleteUiModel,
    onCloseClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = StuColors.Background,
        topBar = {
            ReservationCompleteTopBar(onCloseClick = onCloseClick)
        },
        bottomBar = {
            ReservationCompleteBottomBar(
                onScheduleClick = onScheduleClick,
                onHomeClick = onHomeClick,
            )
        },
    ) { contentPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(horizontal = AppSpacing.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            item {
                Column(
                    modifier = Modifier.padding(top = AppSpacing.xxl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(56.dp)
                                .background(StuColors.Green, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "✓",
                            color = StuColors.White,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }

                    Text(
                        text = "수업 예약 완료!",
                        color = StuColors.TextPrimary,
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                    )

                    Text(
                        text = "${reservation.instructorName}님의 수업 예약이\n성공적으로 완료되었습니다.",
                        color = StuColors.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            item {
                ReservationSummaryCard(reservation = reservation)
            }

            item {
                ReservationNoticeCard(
                    modifier = Modifier.padding(bottom = AppSpacing.sectionGap),
                )
            }
        }
    }
}

@Composable
private fun ReservationCompleteTopBar(
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
            text = "예약 완료",
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
            Text(
                text = "×",
                color = StuColors.TextPrimary,
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
private fun ReservationSummaryCard(
    reservation: ReservationCompleteUiModel,
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
            Box(
                modifier =
                    Modifier
                        .background(StuColors.GreenLight, AppShape.Pill)
                        .padding(
                            horizontal = AppSpacing.chipHorizontalPadding,
                            vertical = AppSpacing.xs,
                        ),
            ) {
                Text(
                    text = reservation.className,
                    color = StuColors.Green,
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            Text(
                text = reservation.dateText,
                color = StuColors.TextPrimary,
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
            )

            Text(
                text = reservation.timeText,
                color = StuColors.TextPrimary,
                style = MaterialTheme.typography.bodyLarge,
            )

            ReservationSummaryRow(
                label = "수업 ID",
                value = reservation.id,
            )

            ReservationSummaryRow(
                label = "강사/장소",
                value = "${reservation.instructorName} / ${reservation.roomName}",
            )
            ReservationSummaryRow(
                label = "사용 수강권",
                value = reservation.classPassName,
            )
            ReservationSummaryRow(
                label = "잔여 횟수",
                value = reservation.remainingCountText,
                valueColor = StuColors.Green,
            )
        }
    }
}

@Composable
private fun ReservationSummaryRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = StuColors.TextPrimary,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            text = label,
            color = StuColors.TextSecondary,
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun ReservationNoticeCard(modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(StuColors.GreenLight, AppShape.Card)
                .padding(AppSpacing.cardPadding),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            text = "▣",
            color = StuColors.Green,
            style = MaterialTheme.typography.bodyMedium,
        )

        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(
                text = "예약 확인 안내",
                color = StuColors.Green,
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = "예약 내역은 마이페이지 > 내 수업 일정에서 언제든지 확인 및 취소가 가능합니다.",
                color = StuColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ReservationCompleteBottomBar(
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

@Preview
@Composable
private fun ReservationCompleteScreenPreview() {
    AppTheme {
        ReservationCompleteScreen(
            reservation =
                ReservationCompleteUiModel(
                    id = "1",
                    className = "리포머 베이직",
                    dateText = "2026.08.08 (토)",
                    timeText = "오전 10:00 - 10:50",
                    instructorName = "이지은 강사",
                    roomName = "A 스튜디오",
                    classPassName = "[그룹] 8:1 리포머/체어",
                    remainingCountText = "2회",
                ),
            onCloseClick = {},
            onScheduleClick = {},
            onHomeClick = {},
        )
    }
}
