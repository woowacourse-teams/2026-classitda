package com.classitda.feature.instructor.management.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import kotlinx.datetime.DayOfWeek

@Composable
internal fun WeekdaySelector(
    selectedDays: Set<DayOfWeek>,
    onDayToggled: (DayOfWeek) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        WEEK_DAYS.forEach { day ->
            val isSelected = day in selectedDays

            Box(
                modifier =
                    Modifier
                        .size(32.dp)
                        .background(
                            color = if (isSelected) InsColors.Black else InsColors.Surface,
                            shape = CircleShape,
                        ).border(
                            width = 1.dp,
                            color = if (isSelected) InsColors.Black else InsColors.Divider,
                            shape = CircleShape,
                        ).clickable { onDayToggled(day) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = day.toKoreanShort(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) InsColors.White else InsColors.TextSecondary,
                )
            }
        }
    }
}

private val WEEK_DAYS =
    listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY,
    )

private fun DayOfWeek.toKoreanShort(): String =
    when (this) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }

@Composable
@Preview
private fun WeekdaySelectorPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        WeekdaySelector(
            selectedDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            onDayToggled = {},
        )
    }
}
