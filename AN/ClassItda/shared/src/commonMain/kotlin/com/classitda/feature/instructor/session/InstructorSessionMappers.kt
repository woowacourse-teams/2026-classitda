package com.classitda.feature.instructor.session

import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.model.instructor.management.ClassSessionStatus
import com.classitda.domain.model.instructor.session.InstructorDailySession
import com.classitda.domain.model.instructor.session.InstructorSessionStatus

internal fun InstructorDailySession.toClassSession() =
    ClassSession(
        id = id,
        classTypeId = classType.id,
        tags = listOf(classType.name),
        title = className,
        startAt = startAt,
        endAt = endAt,
        reservedCount = reservedCount,
        capacity = capacity,
        status = status.toUiStatus(),
    )

internal fun InstructorSessionStatus.toUiStatus() =
    when (this) {
        InstructorSessionStatus.SCHEDULED_BOOKING_OPEN,
        InstructorSessionStatus.SCHEDULED_BOOKING_CLOSED,
        -> ClassSessionStatus.SCHEDULED

        InstructorSessionStatus.COMPLETED -> ClassSessionStatus.COMPLETED

        InstructorSessionStatus.CANCELED -> ClassSessionStatus.CANCELLED

        InstructorSessionStatus.IN_PROGRESS,
        InstructorSessionStatus.UNKNOWN,
        -> ClassSessionStatus.SCHEDULED
    }
