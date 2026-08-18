package com.classitda.feature.student.home.model

data class PendingReservationUiModel(
    val reservationId: String,
    val className: String,
    val instructorName: String,
    val classTimeText: String,
    val remainingMin: Int,
    val remainingProgress: Float,
    val dateText: String,
    val timeRangeText: String,
    val memo: String,
    val passName: String,
    val totalRemainingCount: Int,
    val reservableCount: Int,
    val cancellableCount: Int,
)
