package com.classitda.feature.student.reservation.classreservation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.window.Dialog
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.component.PrimaryButton

internal data class ReservationTimeConflictUiModel(
    val className: String,
    val dateTimeText: String,
    val studioName: String,
)

@Composable
internal fun ReservationTimeConflictDialog(
    conflict: ReservationTimeConflictUiModel,
    onDismissRequest: () -> Unit,
    onScheduleClick: () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = AppShape.Card,
            color = StuColors.White,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(AppSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape).clickable(onClick = onDismissRequest),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "×", color = StuColors.TextSecondary)
                    }
                }

                Box(
                    modifier = Modifier.size(48.dp).background(StuColors.SurfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "▣", color = StuColors.TextTertiary)
                }

                Text(
                    text = "예약 시간이 겹쳐요",
                    color = StuColors.TextPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = "같은 시간에 예약된 수업이 있어\n이 수업을 예약할 수 없어요.",
                    color = StuColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )

                Column(
                    modifier = Modifier.fillMaxWidth().background(StuColors.SurfaceVariant, AppShape.Card)
                        .padding(AppSpacing.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    Text(text = "겹치는 예약", color = StuColors.TextTertiary, style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = conflict.className,
                        color = StuColors.TextPrimary,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(text = conflict.dateTimeText, color = StuColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    Text(text = conflict.studioName, color = StuColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = AppShape.Card,
                    ) {
                        Text(text = "닫기", color = StuColors.TextSecondary)
                    }
                    PrimaryButton(
                        text = "내 일정 보기",
                        onClick = onScheduleClick,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ReservationTimeConflictDialogPreview() {
    AppTheme {
        ReservationTimeConflictDialog(
            conflict =
                ReservationTimeConflictUiModel(
                    className = "리포머 밸런스",
                    dateTimeText = "2026.08.08 (토) 오후 7:30 - 8:20",
                    studioName = "클래스잇다 2호점",
                ),
            onDismissRequest = {},
            onScheduleClick = {},
        )
    }
}
