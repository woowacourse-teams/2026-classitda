package com.classitda.feature.student.reservation.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors

@Composable
internal fun ReservationCalendarFooter(
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            ReservationCalendarLegendItem(
                text = "예약 확정",
                color = StuColors.PrimaryGreen,
            )

            ReservationCalendarLegendItem(
                text = "대기 중",
                color = StuColors.AccentOrange,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        ReservationCalendarTodayButton(
            onClick = onTodayClick,
        )
    }
}

@Composable
private fun ReservationCalendarLegendItem(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ReservationCalendarEventDot(color = color)

        Text(
            text = text,
            color = StuColors.TextSecondary,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun ReservationCalendarTodayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(44.dp)
            .height(28.dp)
            .border(
                width = 1.dp,
                color = StuColors.Divider,
                shape = AppShape.Pill,
            )
            .clip(AppShape.Pill)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "오늘",
            color = StuColors.TextSecondary,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReservationCalendarFooterPreview() {
    AppTheme {
        ReservationCalendarFooter(
            onTodayClick = {},
            modifier = Modifier.padding(AppSpacing.screenPadding),
        )
    }
}
