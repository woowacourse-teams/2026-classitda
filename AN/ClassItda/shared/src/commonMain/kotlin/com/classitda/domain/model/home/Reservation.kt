package com.classitda.domain.model.home

import kotlinx.datetime.LocalDateTime

data class PendingReservation(
    val id: String,
    val className: String,
    val instructorName: String,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val memo: String,
    val pass: ReservationPassSummary,
)

data class ReservationPassSummary(
    val passName: String,
    val totalRemainingCount: Int,
    val reservableCount: Int,
    val cancellableCount: Int,
)
