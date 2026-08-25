package com.classitda.domain.model.instructor.session

import com.classitda.domain.model.studio.StudioId
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

data class InstructorClassType(
    val id: String,
    val name: String,
)

data class InstructorDailySession(
    val id: String,
    val studioId: StudioId,
    val instructorMembershipId: String,
    val instructorName: String,
    val classForm: InstructorClassForm,
    val classType: InstructorClassType,
    val className: String,
    val description: String?,
    val capacity: Int,
    val reservedCount: Int,
    val waitingCount: Int,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val status: InstructorSessionStatus,
    val mine: Boolean,
)

data class InstructorCalendarDay(
    val date: LocalDate,
    val scheduled: Boolean,
    val completed: Boolean,
    val mineScheduled: Boolean,
    val mineCompleted: Boolean,
)

data class InstructorSessionDetail(
    val id: String,
    val studioId: StudioId,
    val instructorMembershipId: String,
    val instructorName: String,
    val classForm: InstructorClassForm,
    val classType: InstructorClassType,
    val className: String,
    val description: String?,
    val capacity: Int,
    val durationMinutes: Int,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val sessionPhase: InstructorSessionStatus,
)

data class InstructorSessionUpdate(
    val classForm: InstructorClassForm,
    val classTypeId: String,
    val className: String,
    val capacity: Int,
    val durationMinutes: Int,
    val startAt: LocalDateTime,
    val description: String?,
)
