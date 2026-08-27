package com.classitda.domain.model.home

import kotlinx.datetime.LocalDateTime

data class UpcomingReservation(
    val id: String,
    val className: String,
    val instructorName: String,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val memo: String,
)
