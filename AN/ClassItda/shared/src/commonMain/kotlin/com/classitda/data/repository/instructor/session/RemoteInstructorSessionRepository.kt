package com.classitda.data.repository.instructor.session

import com.classitda.data.remote.instructor.session.ClassSessionDetailResponseDto
import com.classitda.data.remote.instructor.session.ClassSessionUpdateV1RequestDto
import com.classitda.data.remote.instructor.session.ClassTypeResponseDto
import com.classitda.data.remote.instructor.session.InstructorCalendarResponseDto
import com.classitda.data.remote.instructor.session.InstructorDailySessionResponseDto
import com.classitda.data.remote.instructor.session.InstructorSessionApi
import com.classitda.domain.model.instructor.session.InstructorCalendarDay
import com.classitda.domain.model.instructor.session.InstructorClassForm
import com.classitda.domain.model.instructor.session.InstructorClassType
import com.classitda.domain.model.instructor.session.InstructorDailySession
import com.classitda.domain.model.instructor.session.InstructorSessionDetail
import com.classitda.domain.model.instructor.session.InstructorSessionStatus
import com.classitda.domain.model.instructor.session.InstructorSessionUpdate
import com.classitda.domain.model.studio.StudioId
import com.classitda.domain.repository.instructor.session.InstructorSessionRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

internal class RemoteInstructorSessionRepository(
    private val api: InstructorSessionApi,
) : InstructorSessionRepository {
    override suspend fun getDailySessions(
        studioId: StudioId,
        date: LocalDate,
    ): List<InstructorDailySession> =
        api
            .getDailySessions(studioId.value, date)
            .map { it.toDomain(studioId) }

    override suspend fun getCalendar(
        studioId: StudioId,
        from: LocalDate,
        to: LocalDate,
    ): List<InstructorCalendarDay> =
        api
            .getCalendar(studioId.value, from, to)
            .map(InstructorCalendarResponseDto::toDomain)

    override suspend fun getSession(
        studioId: StudioId,
        sessionId: String,
    ): InstructorSessionDetail =
        api
            .getSession(studioId.value, sessionId)
            .toDomain(studioId)

    override suspend fun updateSession(
        studioId: StudioId,
        sessionId: String,
        update: InstructorSessionUpdate,
    ) {
        api.updateSession(
            studioId = studioId.value,
            sessionId = sessionId,
            request = update.toRequest(),
        )
    }

    override suspend fun cancelSession(
        studioId: StudioId,
        sessionId: String,
    ) {
        api.cancelSession(studioId.value, sessionId)
    }
}

private fun InstructorDailySessionResponseDto.toDomain(studioId: StudioId) =
    InstructorDailySession(
        id = id.toString(),
        studioId = studioId,
        instructorMembershipId = instructorMembershipId.toString(),
        instructorName = instructorName,
        classForm = classForm.toClassForm(),
        classType = classType.toDomain(),
        className = className,
        description = description,
        capacity = capacity,
        reservedCount = reservedCount.toInt(),
        waitingCount = waitingCount.toInt(),
        startAt = LocalDateTime.parse(startAt),
        endAt = LocalDateTime.parse(endAt),
        status = status.toSessionStatus(),
        mine = mine,
    )

private fun InstructorCalendarResponseDto.toDomain() =
    InstructorCalendarDay(
        date = LocalDate.parse(date),
        scheduled = scheduled,
        completed = completed,
        mineScheduled = mineScheduled,
        mineCompleted = mineCompleted,
    )

private fun ClassSessionDetailResponseDto.toDomain(studioId: StudioId) =
    InstructorSessionDetail(
        id = id.toString(),
        studioId = studioId,
        instructorMembershipId = instructorMembershipId.toString(),
        instructorName = instructorName,
        classForm = classForm.toClassForm(),
        classType = classType.toDomain(),
        className = className,
        description = description,
        capacity = capacity,
        durationMinutes = durationMinutes,
        startAt = LocalDateTime.parse(startAt),
        endAt = LocalDateTime.parse(endAt),
        sessionPhase = sessionPhase.toSessionStatus(),
    )

private fun ClassTypeResponseDto.toDomain() =
    InstructorClassType(
        id = id.toString(),
        name = name,
    )

private fun InstructorSessionUpdate.toRequest() =
    ClassSessionUpdateV1RequestDto(
        classForm = classForm.name,
        classTypeId = classTypeId.toLong(),
        className = className,
        capacity = capacity,
        durationMinutes = durationMinutes,
        startAt = startAt.toString(),
        description = description,
    )

private fun String.toClassForm() =
    runCatching { InstructorClassForm.valueOf(this) }
        .getOrDefault(InstructorClassForm.UNKNOWN)

private fun String.toSessionStatus() =
    runCatching { InstructorSessionStatus.valueOf(this) }
        .getOrDefault(InstructorSessionStatus.UNKNOWN)
