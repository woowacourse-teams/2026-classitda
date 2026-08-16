package com.classitda.domain.model.reservation

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

internal data class ReservationPass(
    val id: String,
    val name: String,
    val remainingText: String,
    val expirationText: String,
)
