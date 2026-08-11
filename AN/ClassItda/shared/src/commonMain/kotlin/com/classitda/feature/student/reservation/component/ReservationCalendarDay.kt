package com.classitda.feature.student.reservation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors

@Composable
internal fun ReservationCalendarDay(
    dayOfMonth: Int,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    hasConfirmedReservation: Boolean,
    hasWaitlistReservation: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        val dateModifier = when {
            isSelected -> Modifier.background(
                StuColors.PrimaryGreen,
                CircleShape,
            )

            isToday -> Modifier.border(
                1.dp,
                StuColors.PrimaryGreen,
                CircleShape,
            )

            else -> Modifier
        }

        Box(
            modifier = Modifier
                .size(28.dp)
                .then(dateModifier)
                .clip(CircleShape)
                .clickable(
                    enabled = isCurrentMonth,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = dayOfMonth.toString(),
                color = when {
                    isSelected -> StuColors.White
                    isCurrentMonth -> StuColors.TextPrimary
                    else -> StuColors.TextTertiary
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Row(
            modifier = Modifier.height(6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (hasConfirmedReservation) {
                ReservationCalendarEventDot(
                    color = StuColors.PrimaryGreen,
                )
            }

            if (hasWaitlistReservation) {
                ReservationCalendarEventDot(
                    color = StuColors.AccentOrange,
                )
            }
        }
    }
}

@Composable
internal fun ReservationCalendarEventDot(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(4.dp)
            .background(color, CircleShape),
    )
}

@Preview(name = "선택된 날짜")
@Composable
private fun ReservationCalendarDayPreview1() {
    AppTheme {
        ReservationCalendarDay(
            dayOfMonth = 8,
            isCurrentMonth = true,
            isSelected = true,
            isToday = false,
            hasConfirmedReservation = false,
            hasWaitlistReservation = false,
            onClick = {},
        )
    }
}

@Preview(name = "예약 확정과 대기중")
@Composable
private fun ReservationCalendarDayPreview2() {
    AppTheme {
        ReservationCalendarDay(
            dayOfMonth = 8,
            isCurrentMonth = true,
            isSelected = true,
            isToday = false,
            hasConfirmedReservation = true,
            hasWaitlistReservation = true,
            onClick = {},
        )
    }
}

@Preview(name = "오늘 날짜", showBackground = true)
@Composable
private fun ReservationCalendarDayPreview3() {
    AppTheme {
        ReservationCalendarDay(
            dayOfMonth = 5,
            isCurrentMonth = true,
            isSelected = false,
            isToday = true,
            hasConfirmedReservation = false,
            hasWaitlistReservation = true,
            onClick = {},
        )
    }
}
