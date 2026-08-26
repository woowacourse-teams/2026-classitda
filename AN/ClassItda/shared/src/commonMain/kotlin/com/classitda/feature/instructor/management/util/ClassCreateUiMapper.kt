package com.classitda.feature.instructor.management.util

import com.classitda.domain.model.instructor.management.ClassForm
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.model.instructor.management.ClassSessionStatus
import com.classitda.domain.model.instructor.management.ClassTemplate
import com.classitda.domain.model.instructor.management.ClassTemplateSchedule
import com.classitda.feature.instructor.management.classes.create.model.ClassSessionDraftUiModel
import com.classitda.feature.instructor.management.classtemplates.create.model.ClassTemplateDraftUiModel
import com.classitda.feature.instructor.management.classtemplates.create.model.ClassTemplateFormValues
import com.classitda.feature.instructor.management.model.ClassType
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus

// 실제 API는 반복 여부와 상관없이 startTime을 항상 요구한다. 반복없음일 때는 시작시간 입력 UI가 없으므로 10시로 고정한다.
internal fun ClassTemplateDraftUiModel.toClassTemplate(id: String): ClassTemplate {
    val effectiveStartTime = if (isRepeating) startTime else LocalTime(10, 0)
    return ClassTemplate(
        id = id,
        tags = listOf(classType.label) + categories,
        title = title,
        classForm = classType.toClassForm(),
        durationMinutes = durationMinutes,
        capacity = capacity,
        schedule =
            ClassTemplateSchedule(
                startTime = effectiveStartTime,
                endTime = effectiveStartTime.plusMinutesClamped(durationMinutes),
                repeatDays = if (isRepeating) repeatDays.sortedBy { it.ordinal } else emptyList(),
            ),
        description = description,
        classTypeIds = classTypeIds,
    )
}

internal fun ClassTemplate.toFormValues(): ClassTemplateFormValues =
    ClassTemplateFormValues(
        classType = tags.classTypeOrNull() ?: ClassType.GROUP,
        categories = tags.categoriesOrEmpty(),
        classTypeIds = classTypeIds,
        title = title,
        capacity = capacity,
        durationMinutes = durationMinutes,
        isRepeating = schedule != null,
        repeatDays = schedule?.repeatDays.orEmpty().toSet(),
        startTime = schedule?.startTime ?: LocalTime(10, 0),
        description = description,
    )

internal fun ClassSessionDraftUiModel.toClassSessions(): List<ClassSession> {
    val tags = listOf(classType.label) + categories
    val endTime = startTime.plusMinutesClamped(durationMinutes)

    val dates =
        if (isRepeating) {
            val start = repeatStartDate
            val end = repeatEndDate
            if (start == null || end == null) {
                emptyList()
            } else {
                generateSequence(start) { it.plus(DatePeriod(days = 1)) }
                    .takeWhile { it <= end }
                    .filter { it.dayOfWeek in repeatDays }
                    .toList()
            }
        } else {
            listOfNotNull(sessionDate)
        }

    return dates.map { date ->
        ClassSession(
            id = "",
            tags = tags,
            title = title,
            startAt = LocalDateTime(date, startTime),
            endAt = LocalDateTime(date, endTime),
            reservedCount = 0,
            capacity = capacity,
            status = ClassSessionStatus.SCHEDULED,
        )
    }
}

internal fun ClassType.toClassForm(): ClassForm =
    when (this) {
        ClassType.GROUP -> ClassForm.GROUP
        ClassType.PERSONAL -> ClassForm.INDIVIDUAL
    }

internal fun ClassForm.toClassType(): ClassType =
    when (this) {
        ClassForm.GROUP -> ClassType.GROUP
        ClassForm.INDIVIDUAL -> ClassType.PERSONAL
    }

internal fun List<String>.classTypeOrNull(): ClassType? = ClassType.entries.firstOrNull { it.label in this }

internal fun List<String>.categoriesOrEmpty(): List<String> =
    filter { tag ->
        ClassType.entries.none {
            it.label == tag
        }
    }

internal fun String.digitsOnly(): String = filter { it.isDigit() }

private fun LocalTime.plusMinutesClamped(minutes: Int): LocalTime {
    val totalMinutes = ((hour * 60 + minute + minutes) % (24 * 60) + 24 * 60) % (24 * 60)
    return LocalTime(totalMinutes / 60, totalMinutes % 60)
}
