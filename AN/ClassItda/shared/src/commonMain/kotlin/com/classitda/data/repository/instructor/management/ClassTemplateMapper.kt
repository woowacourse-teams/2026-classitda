package com.classitda.data.repository.instructor.management

import com.classitda.data.remote.dto.ClassFormDto
import com.classitda.data.remote.dto.ClassTemplateCreateRequestDto
import com.classitda.data.remote.dto.ClassTemplateResponseDto
import com.classitda.data.remote.dto.ClassTemplateUpdateRequestDto
import com.classitda.data.remote.dto.ClassTypeResponseDto
import com.classitda.data.remote.dto.RecurringDayDto
import com.classitda.domain.model.instructor.management.ClassForm
import com.classitda.domain.model.instructor.management.ClassTemplate
import com.classitda.domain.model.instructor.management.ClassTemplateSchedule
import com.classitda.domain.model.instructor.management.ClassType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

internal fun ClassTypeResponseDto.toDomain(): ClassType =
    ClassType(
        id = id.toString(),
        name = name,
    )

internal fun ClassTemplateResponseDto.toDomain(): ClassTemplate =
    ClassTemplate(
        id = id.toString(),
        tags = listOf(classType.name),
        title = name,
        classForm = classForm.toDomain(),
        durationMinutes = durationMinutes,
        capacity = capacity,
        schedule = toScheduleOrNull(),
        description = description.orEmpty(),
        classTypeIds = listOf(classType.id.toString()),
    )

private fun ClassTemplateResponseDto.toScheduleOrNull(): ClassTemplateSchedule? {
    if (recurringDays.isEmpty()) return null
    val start = LocalTime.parse(startTime)
    return ClassTemplateSchedule(
        startTime = start,
        endTime = start.plusMinutesClamped(durationMinutes),
        repeatDays = recurringDays.map { it.toDomain() },
    )
}

internal fun ClassTemplate.toCreateRequestDto(): ClassTemplateCreateRequestDto {
    val schedule = requireNotNull(schedule) { "실제 API는 시작 시간이 없는 템플릿을 지원하지 않습니다." }
    return ClassTemplateCreateRequestDto(
        name = title,
        description = description.ifBlank { null },
        classForm = classForm.toDto(),
        durationMinutes = durationMinutes,
        startTime = schedule.startTime.toApiTimeString(),
        recurringDays = schedule.repeatDays.map { it.toDto() },
        capacity = capacity,
        classTypeId = classTypeIds.toSingleRequestId(),
    )
}

internal fun ClassTemplate.toUpdateRequestDto(): ClassTemplateUpdateRequestDto {
    val schedule = requireNotNull(schedule) { "실제 API는 시작 시간이 없는 템플릿을 지원하지 않습니다." }
    return ClassTemplateUpdateRequestDto(
        name = title,
        description = description.ifBlank { null },
        classForm = classForm.toDto(),
        durationMinutes = durationMinutes,
        startTime = schedule.startTime.toApiTimeString(),
        recurringDays = schedule.repeatDays.map { it.toDto() },
        capacity = capacity,
        classTypeId = classTypeIds.toSingleRequestId(),
    )
}

private fun List<String>.toSingleRequestId(): Long {
    val id = singleOrNull() ?: error("수업 종류를 한 개 선택해야 합니다.")
    return id.toClassTypeId()
}

internal fun ClassFormDto.toDomain(): ClassForm =
    when (this) {
        ClassFormDto.INDIVIDUAL -> ClassForm.INDIVIDUAL
        ClassFormDto.GROUP -> ClassForm.GROUP
    }

internal fun ClassForm.toDto(): ClassFormDto =
    when (this) {
        ClassForm.INDIVIDUAL -> ClassFormDto.INDIVIDUAL
        ClassForm.GROUP -> ClassFormDto.GROUP
    }

internal fun RecurringDayDto.toDomain(): DayOfWeek = DayOfWeek.valueOf(name)

internal fun DayOfWeek.toDto(): RecurringDayDto = RecurringDayDto.valueOf(name)

internal fun String.toStudioId(): Long = toLongOrNull() ?: error("올바르지 않은 studioId입니다: $this")

internal fun String.toClassTemplateId(): Long = toLongOrNull() ?: error("올바르지 않은 템플릿 ID입니다: $this")

internal fun String.toClassTypeId(): Long = toLongOrNull() ?: error("올바르지 않은 수업 종류 ID입니다: $this")

internal fun LocalTime.toApiTimeString(): String = "${hour.pad2()}:${minute.pad2()}:${second.pad2()}"

private fun Int.pad2(): String = toString().padStart(2, '0')

internal fun LocalTime.plusMinutesClamped(minutes: Int): LocalTime {
    val totalMinutes = ((hour * 60 + minute + minutes) % (24 * 60) + 24 * 60) % (24 * 60)
    return LocalTime(totalMinutes / 60, totalMinutes % 60)
}
