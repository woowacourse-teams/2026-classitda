package com.classitda.feature.student.reservation.domain.model.reservation

internal data class ReservationClass(
    val id: String,
    val classTime: String,
    val className: String,
    val instructorName: String,
    val roomName: String?,
    val leftStudentCount: Int,
    val isReserved: Boolean = false,
    val isWaitlisted: Boolean = false,
)
