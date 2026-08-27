package com.classitda.feature.student.reservation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors

@Composable
internal fun ReservationCalendarHeader(
    year: Int,
    month: Int,
    isPreviousEnabled: Boolean,
    isMonthMode: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onMonthModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ReservationCalendarMoveButton(
            text = "‹",
            enabled = isPreviousEnabled,
            onClick = onPreviousClick,
        )

        Text(
            text = "${year}년 ${month}월",
            color = StuColors.TextPrimary,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )

        ReservationCalendarMoveButton(
            text = "›",
            enabled = true,
            onClick = onNextClick,
        )

        Spacer(modifier = Modifier.weight(1f))

        ReservationCalendarModeToggle(
            isMonthMode = isMonthMode,
            onMonthModeChange = onMonthModeChange,
        )
    }
}

@Composable
internal fun ReservationCalendarWeekdayHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
    ) {
        listOf("월", "화", "수", "목", "금", "토", "일").forEach { weekday ->
            Text(
                text = weekday,
                color = StuColors.TextTertiary,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ReservationCalendarMoveButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable(
                    enabled = enabled,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (enabled) StuColors.TextSecondary else StuColors.TextTertiary,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
private fun ReservationCalendarModeToggle(
    isMonthMode: Boolean,
    onMonthModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .selectableGroup()
                .background(
                    color = StuColors.Divider,
                    shape = AppShape.Card,
                ).padding(2.dp),
    ) {
        ReservationCalendarModeItem(
            text = "월",
            selected = isMonthMode,
            onClick = {
                onMonthModeChange(true)
            },
        )

        ReservationCalendarModeItem(
            text = "주",
            selected = !isMonthMode,
            onClick = {
                onMonthModeChange(false)
            },
        )
    }
}

@Composable
private fun ReservationCalendarModeItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .width(32.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    color =
                        if (selected) {
                            StuColors.White
                        } else {
                            Color.Transparent
                        },
                ).selectable(
                    selected = selected,
                    role = Role.RadioButton,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color =
                if (selected) {
                    StuColors.TextPrimary
                } else {
                    StuColors.TextSecondary
                },
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Preview(name = "월간 모드")
@Composable
private fun ReservationCalendarHeaderPreview1() {
    AppTheme {
        Column(
            modifier =
                Modifier
                    .background(StuColors.White)
                    .padding(AppSpacing.screenPadding),
        ) {
            ReservationCalendarHeader(
                year = 2026,
                month = 8,
                isPreviousEnabled = false,
                isMonthMode = true,
                onPreviousClick = {},
                onNextClick = {},
                onMonthModeChange = {},
            )

            ReservationCalendarWeekdayHeader(
                modifier = Modifier.padding(top = AppSpacing.md),
            )
        }
    }
}

@Preview(name = "주간 모드")
@Composable
private fun ReservationCalendarHeaderPreview2() {
    AppTheme {
        Column(
            modifier =
                Modifier
                    .background(StuColors.White)
                    .padding(AppSpacing.screenPadding),
        ) {
            ReservationCalendarHeader(
                year = 2026,
                month = 8,
                isPreviousEnabled = false,
                isMonthMode = false,
                onPreviousClick = {},
                onNextClick = {},
                onMonthModeChange = {},
            )

            ReservationCalendarWeekdayHeader(
                modifier = Modifier.padding(top = AppSpacing.md),
            )
        }
    }
}
