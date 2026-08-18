package com.classitda.domain.repository.waitlist

import com.classitda.domain.model.waitlist.WaitlistReservation

internal interface WaitlistReservationRepository {
    fun getWaitlistReservation(classId: String): WaitlistReservation

    fun applyWaitlist(
        classId: String,
        passId: String,
    ): Boolean
}
