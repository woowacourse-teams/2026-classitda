package com.classitda.feature.student.reservation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number

@Composable
internal fun ReservationCalendar(
    year: Int,
    month: Int,
    selectedDayOfMonth: Int,
    todayDayOfMonth: Int?,
    confirmedReservationDays: Set<Int>,
    waitlistReservationDays: Set<Int>,
    isMonthMode: Boolean,
    onDayClick: (Int) -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onMonthModeChange: (Boolean) -> Unit,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayedMonth = YearMonth(
        year = year,
        month = month,
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StuColors.White)
            .padding(
                horizontal = AppSpacing.screenPadding,
                vertical = AppSpacing.cardPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        ReservationCalendarHeader(
            year = year,
            month = month,
            isMonthMode = isMonthMode,
            onPreviousClick = onPreviousClick,
            onNextClick = onNextClick,
            onMonthModeChange = onMonthModeChange,
        )

        ReservationCalendarWeekdayHeader()

        if (isMonthMode) {
            val calendarState = rememberCalendarState(
                startMonth = displayedMonth,
                endMonth = displayedMonth,
                firstVisibleMonth = displayedMonth,
                firstDayOfWeek = DayOfWeek.MONDAY,
                outDateStyle = OutDateStyle.EndOfGrid,
            )

            HorizontalCalendar(
                state = calendarState,
                userScrollEnabled = false,
                dayContent = { day ->
                    val dayOfMonth = day.date.day
                    val isCurrentMonth = day.position == DayPosition.MonthDate

                    ReservationCalendarDay(
                        dayOfMonth = dayOfMonth,
                        isCurrentMonth = isCurrentMonth,
                        isSelected = isCurrentMonth && dayOfMonth == selectedDayOfMonth,
                        isToday = isCurrentMonth && dayOfMonth == todayDayOfMonth,
                        hasConfirmedReservation = isCurrentMonth &&
                            dayOfMonth in confirmedReservationDays,
                        hasWaitlistReservation = isCurrentMonth &&
                            dayOfMonth in waitlistReservationDays,
                        onClick = {
                            onDayClick(dayOfMonth)
                        },
                    )
                },
            )
        } else {
            val selectedDate = LocalDate(
                year = year,
                month = month,
                day = selectedDayOfMonth,
            )
            val weekCalendarState = rememberWeekCalendarState(
                startDate = displayedMonth.firstDay,
                endDate = displayedMonth.lastDay,
                firstVisibleWeekDate = selectedDate,
                firstDayOfWeek = DayOfWeek.MONDAY,
            )

            WeekCalendar(
                state = weekCalendarState,
                userScrollEnabled = false,
                dayContent = { day ->
                    val dayOfMonth = day.date.day
                    val isCurrentMonth = day.date.year == year &&
                        day.date.month.number == month

                    ReservationCalendarDay(
                        dayOfMonth = dayOfMonth,
                        isCurrentMonth = isCurrentMonth,
                        isSelected = isCurrentMonth && dayOfMonth == selectedDayOfMonth,
                        isToday = isCurrentMonth && dayOfMonth == todayDayOfMonth,
                        hasConfirmedReservation = isCurrentMonth &&
                            dayOfMonth in confirmedReservationDays,
                        hasWaitlistReservation = isCurrentMonth &&
                            dayOfMonth in waitlistReservationDays,
                        onClick = {
                            onDayClick(dayOfMonth)
                        },
                    )
                },
            )
        }

        ReservationCalendarFooter(
            onTodayClick = onTodayClick,
        )
    }
}

@Preview(name = "월간 캘린더")
@Composable
private fun ReservationCalendarMonthPreview() {
    AppTheme {
        ReservationCalendar(
            year = 2026,
            month = 8,
            selectedDayOfMonth = 8,
            todayDayOfMonth = 5,
            confirmedReservationDays = setOf(7, 8, 12, 15),
            waitlistReservationDays = setOf(9, 12),
            isMonthMode = true,
            onDayClick = {},
            onPreviousClick = {},
            onNextClick = {},
            onMonthModeChange = {},
            onTodayClick = {},
        )
    }
}

@Preview(name = "주간 캘린더")
@Composable
private fun ReservationCalendarWeekPreview() {
    AppTheme {
        ReservationCalendar(
            year = 2026,
            month = 8,
            selectedDayOfMonth = 8,
            todayDayOfMonth = 5,
            confirmedReservationDays = setOf(7, 8),
            waitlistReservationDays = setOf(9),
            isMonthMode = false,
            onDayClick = {},
            onPreviousClick = {},
            onNextClick = {},
            onMonthModeChange = {},
            onTodayClick = {},
        )
    }
}
