package com.classitda.domain.model.waitlist

internal data class WaitlistReservation(
    val id: String,
    val className: String,
    val dateText: String,
    val timeText: String,
    val instructorName: String,
    val memoText: String,
    val cancellationNotice: String,
    val classPasses: List<WaitlistClassPass>,
    val expectedWaitingNumber: Int,
)

internal data class WaitlistClassPass(
    val id: String,
    val name: String,
    val usageText: String,
    val validityPeriodText: String,
)
