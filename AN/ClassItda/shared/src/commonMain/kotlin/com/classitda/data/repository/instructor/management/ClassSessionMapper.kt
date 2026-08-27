package com.classitda.data.repository.instructor.management

import com.classitda.data.remote.dto.ClassFormDto
import com.classitda.data.remote.dto.ClassSessionCreateRequestDto
import com.classitda.data.remote.dto.ClassSessionStatusDto
import com.classitda.data.remote.dto.ClassSessionUpdateRequestDto
import com.classitda.data.remote.dto.InstructorDailySessionResponseDto
import com.classitda.domain.model.instructor.management.ClassForm
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.model.instructor.management.ClassSessionCreateRequest
import com.classitda.domain.model.instructor.management.ClassSessionStatus
import kotlinx.datetime.LocalDateTime

internal fun InstructorDailySessionResponseDto.toDomain(): ClassSession =
    ClassSession(
        id = id.toString(),
        classTypeId = classType.id.toString(),
        tags = listOf(classForm.toTagLabel(), classType.name),
        title = className,
        startAt = LocalDateTime.parse(startAt),
        endAt = LocalDateTime.parse(endAt),
        reservedCount = reservedCount.toInt(),
        capacity = capacity,
        status = status.toDomain(),
    )

internal fun ClassSessionCreateRequest.toRequestDto(): ClassSessionCreateRequestDto =
    ClassSessionCreateRequestDto(
        classForm = classForm.toDto(),
        classTypeId = classTypeId.toClassTypeId(),
        className = title,
        capacity = capacity,
        durationMinutes = durationMinutes,
        recurring = recurring,
        startTime = startTime.toApiTimeString(),
        description = description.ifBlank { null },
        classDate = classDate?.toString(),
        recurringDays = recurringDays.map { it.toDto() }.takeIf { recurring },
        repeatStartDate = repeatStartDate?.toString(),
        repeatEndDate = repeatEndDate?.toString(),
    )

// ClassSession은 classForm을 별도로 들고 있지 않고 tags[0]에 라벨만 담아두므로(기존 edit 화면과 동일한 관례) 여기서 역으로 매칭한다.
internal fun ClassSession.toUpdateRequestDto(): ClassSessionUpdateRequestDto =
    ClassSessionUpdateRequestDto(
        classForm = tags.toClassForm().toDto(),
        classTypeId = classTypeId.toClassTypeId(),
        className = title,
        capacity = capacity,
        durationMinutes = durationBetween(startAt, endAt),
        startAt = "${startAt.date}T${startAt.time.toApiTimeString()}",
    )

internal fun String.toClassSessionId(): Long = toLongOrNull() ?: error("올바르지 않은 수업 회차 ID입니다: $this")

private fun List<String>.toClassForm(): ClassForm = if ("개인 수업" in this) ClassForm.INDIVIDUAL else ClassForm.GROUP

private fun ClassFormDto.toTagLabel(): String =
    when (this) {
        ClassFormDto.GROUP -> "그룹 수업"
        ClassFormDto.INDIVIDUAL -> "개인 수업"
    }

private fun ClassSessionStatusDto.toDomain(): ClassSessionStatus =
    when (this) {
        ClassSessionStatusDto.SCHEDULED_BOOKING_OPEN,
        ClassSessionStatusDto.SCHEDULED_BOOKING_CLOSED,
        ClassSessionStatusDto.IN_PROGRESS,
        -> ClassSessionStatus.SCHEDULED

        ClassSessionStatusDto.COMPLETED -> ClassSessionStatus.COMPLETED

        ClassSessionStatusDto.CANCELED -> ClassSessionStatus.CANCELLED
    }

private fun durationBetween(
    start: LocalDateTime,
    end: LocalDateTime,
): Int {
    val startMinutes = start.time.hour * 60 + start.time.minute
    val endMinutes = end.time.hour * 60 + end.time.minute
    return (endMinutes - startMinutes).takeIf { it > 0 } ?: 50
}
