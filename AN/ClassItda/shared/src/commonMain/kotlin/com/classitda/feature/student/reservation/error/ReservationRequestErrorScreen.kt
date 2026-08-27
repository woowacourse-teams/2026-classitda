package com.classitda.feature.student.reservation.error

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.component.PrimaryButton

@Composable
internal fun ReservationRequestErrorScreen(
    title: String,
    message: String,
    onReservationClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(StuColors.Background)
                .padding(horizontal = AppSpacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier.size(56.dp).background(StuColors.SurfaceVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "!", color = StuColors.TextTertiary, style = MaterialTheme.typography.titleLarge)
        }

        Column(
            modifier = Modifier.padding(top = AppSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = title,
                color = StuColors.TextPrimary,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                color = StuColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            PrimaryButton(
                text = "예약 화면으로 이동",
                onClick = onReservationClick,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedButton(
                onClick = onHomeClick,
                modifier = Modifier.fillMaxWidth(),
                shape = AppShape.Card,
            ) {
                Text(text = "홈으로 이동", color = StuColors.TextSecondary)
            }
        }
    }
}

@Preview
@Composable
private fun ReservationRequestErrorScreenPreview() {
    AppTheme {
        ReservationRequestErrorScreen(
            title = "수업 대기 요청이 실패했습니다.",
            message = "대기 신청 가능 시간이 종료되었습니다.",
            onReservationClick = {},
            onHomeClick = {},
        )
    }
}
