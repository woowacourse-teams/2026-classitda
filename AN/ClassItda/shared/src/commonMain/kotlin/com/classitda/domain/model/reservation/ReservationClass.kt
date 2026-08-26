package com.classitda.domain.model.reservation

import kotlinx.datetime.LocalDate

internal data class ReservationClass(
    val id: String,
    val date: LocalDate,
    val classTime: String,
    val className: String,
    val instructorName: String,
    val description: String?,
    val leftStudentCount: Int,
    val isReserved: Boolean = false,
    val isWaitlisted: Boolean = false,
)

internal data class ReservationPass(
    val id: String,
    val name: String,
    val remainingText: String,
    val validityPeriodText: String,
)
