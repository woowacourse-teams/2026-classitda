package com.classitda.feature.student.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_clock
import classitda.shared.generated.resources.ic_person
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.feature.student.home.component.PrimaryTextButton
import org.jetbrains.compose.resources.painterResource

@Composable
fun PendingReservationCard(
    className: String,
    instructorName: String,
    classTime: String,
    remainingMin: Int,
    remainingProgress: Float,
    onLaterClick: () -> Unit,
    onApproveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(StuColors.Surface, AppShape.Card)
                .padding(AppSpacing.cardPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier,
            ) {
                Text(
                    text = "대기하던 수업에 자리가 났어요!",
                    style = MaterialTheme.typography.labelSmall,
                )

                Spacer(modifier = Modifier.height(AppSpacing.md))

                Text(
                    text = className,
                    style =
                        MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = StuColors.TextPrimary,
                )
                Spacer(modifier = Modifier.height(AppSpacing.lg))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardItemHorizontalGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_person),
                        contentDescription = "강사",
                        tint = StuColors.TextSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "$instructorName 강사",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StuColors.TextSecondary,
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.cardItemVerticalGap))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardItemHorizontalGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_clock),
                        contentDescription = "시간",
                        tint = StuColors.TextSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = classTime,
                        style = MaterialTheme.typography.bodyMedium,
                        color = StuColors.TextSecondary,
                    )
                }
            }

            RemainingProgressIndicator(
                remainingMin = remainingMin,
                remainingProgress = remainingProgress,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.lg))

        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
            Button(
                onClick = onLaterClick,
                modifier = Modifier.weight(1f),
                shape = AppShape.Card,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = StuColors.TextSecondary,
                    ),
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                Text(text = "다음에")
            }
            PrimaryTextButton(
                content = "예약 승인",
                onClick = onApproveClick,
                modifier = Modifier.weight(2f),
                contentPadding = PaddingValues(vertical = 16.dp),
            )
        }
    }
}

@Composable
private fun RemainingProgressIndicator(
    remainingMin: Int,
    remainingProgress: Float,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(64.dp)) {
        CircularProgressIndicator(
            progress = { remainingProgress },
            modifier = Modifier.size(64.dp),
            color = StuColors.Primary,
            strokeWidth = 4.dp,
            trackColor = StuColors.SurfaceVariant,
            strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
        )
        Text(
            text = "${remainingMin}분",
            style = MaterialTheme.typography.labelLarge,
            color = StuColors.Primary,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
@Preview
private fun PendingReservationCardPreview() {
    AppTheme {
        PendingReservationCard(
            className = "리포머 밸런스",
            instructorName = "이지은",
            classTime = "오늘 오후 7:30",
            remainingMin = 18,
            remainingProgress = 18 / 60f,
            onLaterClick = {},
            onApproveClick = {},
        )
    }
}
