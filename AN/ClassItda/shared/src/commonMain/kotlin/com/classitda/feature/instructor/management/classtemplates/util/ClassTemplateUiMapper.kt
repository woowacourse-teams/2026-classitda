package com.classitda.feature.instructor.management.classtemplates.util

import com.classitda.domain.model.instructor.management.ClassTemplate
import com.classitda.domain.model.instructor.management.ClassTemplateSchedule
import com.classitda.feature.instructor.management.classtemplates.model.ClassScheduleUiModel
import com.classitda.feature.instructor.management.classtemplates.model.ClassTemplateUiModel
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

internal fun ClassTemplate.toUiModel(): ClassTemplateUiModel =
    ClassTemplateUiModel(
        id = id,
        classForm = classForm,
        classTypeId = classTypeId,
        categoryNames = tags,
        title = title,
        durationText = "${durationMinutes}분",
        capacityText = "${capacity}명",
        schedule = schedule?.toUiModel(),
    )

private fun ClassTemplateSchedule.toUiModel(): ClassScheduleUiModel =
    ClassScheduleUiModel(
        timeRangeText = "${formatTime24(startTime)} ~ ${formatTime24(endTime)}",
        repeatDaysText = repeatDays.sortedBy { it.ordinal }.joinToString(", ") { it.toKoreanShort() },
    )

private fun formatTime24(time: LocalTime): String =
    "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"

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
