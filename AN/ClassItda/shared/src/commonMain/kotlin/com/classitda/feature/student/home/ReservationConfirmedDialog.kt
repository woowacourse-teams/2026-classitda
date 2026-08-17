package com.classitda.feature.student.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_check_circle
import classitda.shared.generated.resources.ic_close
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.feature.student.home.component.PrimaryTextButton
import org.jetbrains.compose.resources.painterResource

@Composable
fun ReservationConfirmedDialog(
    className: String,
    date: String,
    timeRange: String,
    onCheckScheduleClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        ReservationConfirmedDialogContent(
            className = className,
            date = date,
            timeRange = timeRange,
            onCheckScheduleClick = onCheckScheduleClick,
            onDismissRequest = onDismissRequest,
        )
    }
}

@Composable
private fun ReservationConfirmedDialogContent(
    className: String,
    date: String,
    timeRange: String,
    onCheckScheduleClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.Surface, AppShape.Card)
                .padding(AppSpacing.xl),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = "닫기",
                tint = StuColors.TextSecondary,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .clickable(onClick = onDismissRequest)
                        .size(24.dp),
            )
        }

        Spacer(Modifier.height(AppSpacing.md))

        Icon(
            painter = painterResource(Res.drawable.ic_check_circle),
            contentDescription = null,
            tint = StuColors.Primary,
            modifier = Modifier.size(48.dp).align(Alignment.CenterHorizontally),
        )

        Spacer(Modifier.height(AppSpacing.lg))

        Text(
            text = "예약이 확정되었습니다",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(
            Modifier.height(
                AppSpacing.sectionGap,
            ),
        )

        Text(
            text = className,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(AppSpacing.sm))

        Text(
            text = date,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = StuColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(AppSpacing.sm))

        Text(
            text = timeRange,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = StuColors.TextSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(AppSpacing.sectionGap))

        Text(
            text = "변경 및 취소는 마이페이지에서 가능합니다.",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = StuColors.TextTertiary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(AppSpacing.sectionGap))

        PrimaryTextButton(
            content = "내 일정 확인하기",
            onClick = onCheckScheduleClick,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 16.dp),
        )
    }
}

@Composable
@Preview
private fun ReservationConfirmedDialogPreview() {
    AppTheme {
        ReservationConfirmedDialogContent(
            className = "리포머 밸런스",
            date = "2026.08.05(목)",
            timeRange = "오후 7:30 ~ 8:40",
            onCheckScheduleClick = {},
            onDismissRequest = {},
        )
    }
}
