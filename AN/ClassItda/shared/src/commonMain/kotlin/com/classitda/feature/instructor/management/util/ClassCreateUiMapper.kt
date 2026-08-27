package com.classitda.feature.instructor.management.util

import com.classitda.domain.model.instructor.management.ClassForm
import com.classitda.domain.model.instructor.management.ClassSessionCreateRequest
import com.classitda.domain.model.instructor.management.ClassTemplate
import com.classitda.domain.model.instructor.management.ClassTemplateSchedule
import com.classitda.domain.model.instructor.management.ClassType
import com.classitda.feature.instructor.management.classes.create.model.ClassSessionDraftUiModel
import com.classitda.feature.instructor.management.classtemplates.create.model.ClassTemplateDraftUiModel
import com.classitda.feature.instructor.management.classtemplates.create.model.ClassTemplateFormValues
import com.classitda.feature.instructor.management.model.ClassFormOption
import kotlinx.datetime.LocalTime

// 실제 API는 반복 여부와 상관없이 startTime을 항상 요구한다. 반복없음일 때는 시작시간 입력 UI가 없으므로 10시로 고정한다.
internal fun ClassTemplateDraftUiModel.toClassTemplate(id: String): ClassTemplate {
    val effectiveStartTime = if (isRepeating) startTime else LocalTime(10, 0)
    return ClassTemplate(
        id = id,
        tags = listOfNotNull(classType.label, category?.name),
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
        classTypeId = category?.id,
    )
}

internal fun ClassTemplate.toFormValues(classTypes: List<ClassType>): ClassTemplateFormValues =
    ClassTemplateFormValues(
        classType = tags.classFormOptionOrNull() ?: ClassFormOption.GROUP,
        category = classTypes.firstOrNull { it.id == classTypeId },
        title = title,
        capacity = capacity,
        durationMinutes = durationMinutes,
        isRepeating = schedule != null,
        repeatDays = schedule?.repeatDays.orEmpty().toSet(),
        startTime = schedule?.startTime ?: LocalTime(10, 0),
        description = description,
    )

internal fun ClassSessionDraftUiModel.toCreateRequest(classTypeId: String): ClassSessionCreateRequest =
    ClassSessionCreateRequest(
        classForm = classType.toClassForm(),
        classTypeId = classTypeId,
        title = title,
        capacity = capacity,
        durationMinutes = durationMinutes,
        startTime = startTime,
        description = description,
        recurring = isRepeating,
        classDate = if (isRepeating) null else sessionDate,
        recurringDays = if (isRepeating) repeatDays.toList() else emptyList(),
        repeatStartDate = if (isRepeating) repeatStartDate else null,
        repeatEndDate = if (isRepeating) repeatEndDate else null,
    )

internal fun ClassFormOption.toClassForm(): ClassForm =
    when (this) {
        ClassFormOption.GROUP -> ClassForm.GROUP
        ClassFormOption.PERSONAL -> ClassForm.INDIVIDUAL
    }

internal fun ClassForm.toClassFormOption(): ClassFormOption =
    when (this) {
        ClassForm.GROUP -> ClassFormOption.GROUP
        ClassForm.INDIVIDUAL -> ClassFormOption.PERSONAL
    }

internal fun List<String>.classFormOptionOrNull(): ClassFormOption? =
    ClassFormOption.entries.firstOrNull {
        it.label in
            this
    }

internal fun String.digitsOnly(): String = filter { it.isDigit() }

private fun LocalTime.plusMinutesClamped(minutes: Int): LocalTime {
    val totalMinutes = ((hour * 60 + minute + minutes) % (24 * 60) + 24 * 60) % (24 * 60)
    return LocalTime(totalMinutes / 60, totalMinutes % 60)
}
