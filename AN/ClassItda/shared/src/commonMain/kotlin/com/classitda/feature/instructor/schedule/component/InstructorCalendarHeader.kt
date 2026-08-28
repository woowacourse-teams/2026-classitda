package com.classitda.feature.instructor.schedule.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.InsColors

@Composable
internal fun InstructorCalendarHeader(
    year: Int,
    month: Int,
    isMonthMode: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        InstructorCalendarMoveButton("‹", onPreviousMonth)
        Text(
            text = "${year}년 ${month}월",
            color = InsColors.TextPrimary,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )
        InstructorCalendarMoveButton("›", onNextMonth)
        Spacer(Modifier.weight(1f))
        Box(
            modifier =
                Modifier
                    .width(96.dp)
                    .height(48.dp)
                    .selectableGroup(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .width(96.dp)
                        .height(40.dp)
                        .background(InsColors.Divider, RoundedCornerShape(10.dp)),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                InstructorCalendarModeItem("월", isMonthMode) { onModeChange(true) }
                InstructorCalendarModeItem("주", !isMonthMode) { onModeChange(false) }
            }
        }
    }
}

@Composable
private fun InstructorCalendarMoveButton(
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .width(48.dp)
                .height(48.dp)
                .selectable(
                    selected = false,
                    role = Role.Button,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = InsColors.TextSecondary, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun InstructorCalendarModeItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .width(48.dp)
                .height(48.dp)
                .selectable(
                    selected = selected,
                    role = Role.RadioButton,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .width(40.dp)
                    .height(32.dp)
                    .background(
                        color = if (selected) InsColors.White else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = if (selected) InsColors.TextPrimary else InsColors.TextSecondary,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
internal fun InstructorCalendarWeekdayHeader(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        listOf("월", "화", "수", "목", "금", "토", "일").forEach { weekday ->
            Text(
                text = weekday,
                color = InsColors.TextTertiary,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
