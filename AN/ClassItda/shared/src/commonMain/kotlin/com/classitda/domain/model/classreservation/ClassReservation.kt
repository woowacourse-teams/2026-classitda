package com.classitda.domain.model.classreservation

internal data class ClassReservation(
    val id: String,
    val className: String,
    val dateText: String,
    val timeText: String,
    val instructorName: String,
    val roomName: String,
    val cancellationNotice: String,
    val classPasses: List<ClassPass>,
)

internal data class ClassPass(
    val id: String,
    val name: String,
    val usageText: String,
    val expirationText: String,
)
