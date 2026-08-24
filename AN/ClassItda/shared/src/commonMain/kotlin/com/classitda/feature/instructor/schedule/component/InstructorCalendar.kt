package com.classitda.feature.instructor.schedule.component

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_back
import classitda.shared.generated.resources.ic_arrow_forward
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun InstructorCalendar(
    displayedYear: Int,
    displayedMonth: Int,
    selectedDate: LocalDate,
    isMonthMode: Boolean,
    scheduledDates: Set<LocalDate>,
    completedDates: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
    onModeChange: (Boolean) -> Unit,
    onTodayClick: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    val calendarMonth = YearMonth(displayedYear, Month.entries[displayedMonth - 1])
    val firstDay = calendarMonth.firstDay
    val monthDays =
        buildList<LocalDate?> {
            repeat(firstDay.dayOfWeek.ordinal) { add(null) }
            repeat(calendarMonth.lastDay.day) { add(firstDay.plus(DatePeriod(days = it))) }
            while (size % 7 != 0) add(null)
        }
    val weekStart = selectedDate - DatePeriod(days = selectedDate.dayOfWeek.ordinal)
    val weekDays = List(7) { index -> weekStart.plus(DatePeriod(days = index)) }

    Column(
        modifier = Modifier.fillMaxWidth().background(InsColors.White).padding(AppSpacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_back),
                contentDescription = "이전 달",
                tint = InsColors.TextSecondary,
                modifier = Modifier.size(20.dp).clickable(onClick = onPreviousMonth),
            )
            Spacer(Modifier.width(AppSpacing.sm))
            Text(
                text = "${displayedYear}년 ${displayedMonth}월",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.width(AppSpacing.sm))
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_forward),
                contentDescription = "다음 달",
                tint = InsColors.TextSecondary,
                modifier = Modifier.size(20.dp).clickable(onClick = onNextMonth),
            )
            Spacer(Modifier.weight(1f))
            Surface(shape = AppShape.Card, color = InsColors.SurfaceVariant) {
                Row(Modifier.padding(2.dp)) {
                    Surface(
                        shape = AppShape.Card,
                        color = if (isMonthMode) InsColors.White else Color.Transparent,
                        modifier = Modifier.clickable { onModeChange(true) },
                    ) {
                        Text(
                            text = "월",
                            color = if (isMonthMode) InsColors.TextPrimary else InsColors.TextSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier =
                                Modifier.padding(
                                    horizontal = AppSpacing.sm,
                                    vertical = AppSpacing.xs,
                                ),
                        )
                    }
                    Surface(
                        shape = AppShape.Card,
                        color = if (isMonthMode) Color.Transparent else InsColors.White,
                        modifier = Modifier.clickable { onModeChange(false) },
                    ) {
                        Text(
                            text = "주",
                            color = if (isMonthMode) InsColors.TextSecondary else InsColors.TextPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier =
                                Modifier.padding(
                                    horizontal = AppSpacing.sm,
                                    vertical = AppSpacing.xs,
                                ),
                        )
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            listOf("월", "화", "수", "목", "금", "토", "일").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    color = InsColors.TextTertiary,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                )
            }
        }
        val visibleWeeks: List<List<LocalDate?>> = if (isMonthMode) monthDays.chunked(7) else listOf(weekDays)
        visibleWeeks.forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    InstructorCalendarDay(
                        date = date,
                        isSelected = date == selectedDate,
                        hasScheduledSession = date in scheduledDates,
                        hasCompletedSession = date in completedDates,
                        onDateSelected = onDateSelected,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            InstructorCalendarLegendDot(InsColors.Purple)
            Text(
                text = "예정 수업",
                color = InsColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.width(AppSpacing.xxl))
            InstructorCalendarLegendDot(InsColors.Gray400)
            Text(
                text = "완료 수업",
                color = InsColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "오늘",
                color = InsColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable(onClick = onTodayClick),
            )
        }
    }
}

@Composable
private fun InstructorCalendarDay(
    date: LocalDate?,
    isSelected: Boolean,
    hasScheduledSession: Boolean,
    hasCompletedSession: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .height(40.dp)
                .clip(AppShape.Pill)
                .clickable(enabled = date != null) { date?.let(onDateSelected) },
        contentAlignment = Alignment.Center,
    ) {
        if (date != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier =
                        Modifier
                            .size(28.dp)
                            .clip(AppShape.Pill)
                            .background(if (isSelected) InsColors.Black else Color.Transparent),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = date.day.toString(),
                        color = if (isSelected) InsColors.White else InsColors.TextPrimary,
                    )
                }
                Row(Modifier.height(6.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (hasScheduledSession) InstructorCalendarLegendDot(InsColors.Purple)
                    if (hasCompletedSession) InstructorCalendarLegendDot(InsColors.Gray400)
                }
            }
        }
    }
}

@Composable
private fun InstructorCalendarLegendDot(color: Color) {
    Box(Modifier.size(4.dp).clip(AppShape.Pill).background(color))
}

internal val DayOfWeek.koreanName: String
    get() =
        when (this) {
            DayOfWeek.MONDAY -> "월요일"
            DayOfWeek.TUESDAY -> "화요일"
            DayOfWeek.WEDNESDAY -> "수요일"
            DayOfWeek.THURSDAY -> "목요일"
            DayOfWeek.FRIDAY -> "금요일"
            DayOfWeek.SATURDAY -> "토요일"
            DayOfWeek.SUNDAY -> "일요일"
        }

@Preview(name = "강사 일정 캘린더 - 월간", showBackground = true, widthDp = 390)
@Composable
private fun InstructorCalendarMonthPreview() {
    AppTheme(theme = com.classitda.core.designsystem.ThemeType.INSTRUCTOR) {
        InstructorCalendar(
            displayedYear = 2026,
            displayedMonth = 8,
            selectedDate = LocalDate(2026, 8, 5),
            isMonthMode = true,
            scheduledDates = setOf(LocalDate(2026, 8, 5), LocalDate(2026, 8, 6)),
            completedDates = setOf(LocalDate(2026, 8, 4)),
            onDateSelected = {},
            onModeChange = {},
            onTodayClick = {},
            onPreviousMonth = {},
            onNextMonth = {},
        )
    }
}

@Preview(name = "강사 일정 캘린더 - 주간", showBackground = true, widthDp = 390)
@Composable
private fun InstructorCalendarWeekPreview() {
    AppTheme(theme = com.classitda.core.designsystem.ThemeType.INSTRUCTOR) {
        InstructorCalendar(
            displayedYear = 2026,
            displayedMonth = 8,
            selectedDate = LocalDate(2026, 8, 5),
            isMonthMode = false,
            scheduledDates = setOf(LocalDate(2026, 8, 5), LocalDate(2026, 8, 6)),
            completedDates = setOf(LocalDate(2026, 8, 4)),
            onDateSelected = {},
            onModeChange = {},
            onTodayClick = {},
            onPreviousMonth = {},
            onNextMonth = {},
        )
    }
}
