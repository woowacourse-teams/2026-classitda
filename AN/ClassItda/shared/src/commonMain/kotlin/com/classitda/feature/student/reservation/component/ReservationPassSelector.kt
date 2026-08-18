package com.classitda.feature.student.reservation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_expand_more
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.feature.student.reservation.contract.ReservationPassUiModel
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ReservationPassSelector(
    selectedPass: ReservationPassUiModel?,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(StuColors.White)
                .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.sm)
                .clip(RoundedCornerShape(12.dp))
                .background(StuColors.SurfaceVariant)
                .clickable(onClick = onClick)
                .padding(horizontal = AppSpacing.lg, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = selectedPass?.name ?: "수강권 선택",
            color = StuColors.TextSecondary,
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(modifier = Modifier.weight(1f))
        selectedPass?.let { pass ->
            Text(
                text = pass.remainingText.toRemainingCountText(),
                color = StuColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.width(AppSpacing.sm))
        }
        Icon(
            painter = painterResource(Res.drawable.ic_expand_more),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = StuColors.TextTertiary,
        )
    }
}

private fun String.toRemainingCountText(): String =
    substringAfter("잔여 ", missingDelimiterValue = this)
        .substringBefore(" /")
        .let { "$it 남음" }

@Preview(name = "수강권 선택 바 - 선택 전", showBackground = true)
@Composable
private fun ReservationPassSelectorEmptyPreview() {
    AppTheme {
        ReservationPassSelector(
            selectedPass = null,
            onClick = {},
        )
    }
}

@Preview(name = "수강권 선택 바 - 선택 완료", showBackground = true)
@Composable
private fun ReservationPassSelectorSelectedPreview() {
    AppTheme {
        ReservationPassSelector(
            selectedPass = ReservationPassUiModel(
                id = "pass-1",
                name = "요가 10회권",
                remainingText = "잔여 7회 / 예약 가능 7회",
                validityPeriodText = "유효기간: 2026.08.01 ~ 2026.10.31",
            ),
            onClick = {},
        )
    }
}
