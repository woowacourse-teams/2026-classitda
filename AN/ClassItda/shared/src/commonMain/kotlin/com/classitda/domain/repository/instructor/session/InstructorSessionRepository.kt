package com.classitda.domain.repository.instructor.session

import com.classitda.domain.model.instructor.session.InstructorCalendarDay
import com.classitda.domain.model.instructor.session.InstructorDailySession
import com.classitda.domain.model.instructor.session.InstructorSessionDetail
import com.classitda.domain.model.instructor.session.InstructorSessionUpdate
import com.classitda.domain.model.studio.StudioId
import kotlinx.datetime.LocalDate

interface InstructorSessionRepository {
    suspend fun getDailySessions(
        studioId: StudioId,
        date: LocalDate,
    ): List<InstructorDailySession>

    suspend fun getCalendar(
        studioId: StudioId,
        from: LocalDate,
        to: LocalDate,
    ): List<InstructorCalendarDay>

    suspend fun getSession(
        studioId: StudioId,
        sessionId: String,
    ): InstructorSessionDetail

    suspend fun updateSession(
        studioId: StudioId,
        sessionId: String,
        update: InstructorSessionUpdate,
    )

    suspend fun cancelSession(
        studioId: StudioId,
        sessionId: String,
    )
}
