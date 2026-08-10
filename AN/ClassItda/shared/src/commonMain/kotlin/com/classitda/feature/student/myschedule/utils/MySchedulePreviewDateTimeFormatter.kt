package com.classitda.feature.student.myschedule.utils

import androidx.compose.runtime.Composable
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_clock_time
import classitda.shared.generated.resources.my_schedule_date_label
import classitda.shared.generated.resources.my_schedule_date_time_label
import classitda.shared.generated.resources.my_schedule_history_time_range_different_period
import classitda.shared.generated.resources.my_schedule_history_time_range_same_period
import classitda.shared.generated.resources.my_schedule_month_label
import classitda.shared.generated.resources.my_schedule_period_am
import classitda.shared.generated.resources.my_schedule_period_pm
import classitda.shared.generated.resources.my_schedule_upcoming_time_range_different_period
import classitda.shared.generated.resources.my_schedule_upcoming_time_range_same_period
import classitda.shared.generated.resources.my_schedule_weekday_friday
import classitda.shared.generated.resources.my_schedule_weekday_monday
import classitda.shared.generated.resources.my_schedule_weekday_saturday
import classitda.shared.generated.resources.my_schedule_weekday_sunday
import classitda.shared.generated.resources.my_schedule_weekday_thursday
import classitda.shared.generated.resources.my_schedule_weekday_tuesday
import classitda.shared.generated.resources.my_schedule_weekday_wednesday
import com.classitda.feature.student.myschedule.contract.HistoryScheduleDateTimeUiModel
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleDateTimeUiModel
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

private val previewScheduleTimeZone = TimeZone.of("Asia/Seoul")

@Composable
internal fun previewUpcomingScheduleDateTime(
    startAt: Instant,
    endAt: Instant,
): UpcomingScheduleDateTimeUiModel {
    val start = startAt.toLocalDateTime(previewScheduleTimeZone)
    val end = endAt.toLocalDateTime(previewScheduleTimeZone)

    return UpcomingScheduleDateTimeUiModel(
        sectionDateLabel = scheduleDateLabel(start),
        timeRangeLabel =
            scheduleTimeRangeLabel(
                start = start,
                end = end,
                samePeriodResource = Res.string.my_schedule_upcoming_time_range_same_period,
                differentPeriodResource = Res.string.my_schedule_upcoming_time_range_different_period,
            ),
    )
}

@Composable
internal fun previewHistoryScheduleDateTime(
    startAt: Instant,
    endAt: Instant,
): HistoryScheduleDateTimeUiModel {
    val start = startAt.toLocalDateTime(previewScheduleTimeZone)
    val end = endAt.toLocalDateTime(previewScheduleTimeZone)
    val dateLabel = scheduleDateLabel(start)
    val timeRangeLabel =
        scheduleTimeRangeLabel(
            start = start,
            end = end,
            samePeriodResource = Res.string.my_schedule_history_time_range_same_period,
            differentPeriodResource = Res.string.my_schedule_history_time_range_different_period,
        )

    return HistoryScheduleDateTimeUiModel(
        monthLabel = stringResource(Res.string.my_schedule_month_label, start.year, start.month.number),
        dateTimeLabel = stringResource(Res.string.my_schedule_date_time_label, dateLabel, timeRangeLabel),
    )
}

@Composable
private fun scheduleDateLabel(dateTime: LocalDateTime): String =
    stringResource(
        Res.string.my_schedule_date_label,
        dateTime.month.number,
        dateTime.day,
        stringResource(scheduleWeekdayResource(dateTime.dayOfWeek)),
    )

@Composable
private fun scheduleTimeRangeLabel(
    start: LocalDateTime,
    end: LocalDateTime,
    samePeriodResource: StringResource,
    differentPeriodResource: StringResource,
): String {
    val startPeriod = schedulePeriodLabel(start.hour)
    val endPeriod = schedulePeriodLabel(end.hour)
    val startTime = scheduleClockTimeLabel(start)
    val endTime = scheduleClockTimeLabel(end)

    return if (startPeriod == endPeriod) {
        stringResource(samePeriodResource, startPeriod, startTime, endTime)
    } else {
        stringResource(differentPeriodResource, startPeriod, startTime, endPeriod, endTime)
    }
}

@Composable
private fun schedulePeriodLabel(hour: Int): String =
    stringResource(
        if (hour < 12) {
            Res.string.my_schedule_period_am
        } else {
            Res.string.my_schedule_period_pm
        },
    )

@Composable
private fun scheduleClockTimeLabel(dateTime: LocalDateTime): String {
    val hourInTwelveHourClock = dateTime.hour % 12
    val hour = hourInTwelveHourClock.takeUnless { it == 0 } ?: 12
    val minute = dateTime.minute.toString().padStart(length = 2, padChar = '0')
    return stringResource(Res.string.my_schedule_clock_time, hour, minute)
}

private fun scheduleWeekdayResource(dayOfWeek: DayOfWeek): StringResource =
    when (dayOfWeek) {
        DayOfWeek.MONDAY -> Res.string.my_schedule_weekday_monday
        DayOfWeek.TUESDAY -> Res.string.my_schedule_weekday_tuesday
        DayOfWeek.WEDNESDAY -> Res.string.my_schedule_weekday_wednesday
        DayOfWeek.THURSDAY -> Res.string.my_schedule_weekday_thursday
        DayOfWeek.FRIDAY -> Res.string.my_schedule_weekday_friday
        DayOfWeek.SATURDAY -> Res.string.my_schedule_weekday_saturday
        DayOfWeek.SUNDAY -> Res.string.my_schedule_weekday_sunday
    }
