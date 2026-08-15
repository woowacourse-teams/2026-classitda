package com.classitda.feature.student.reservation.domain.repository.waitlist

import com.classitda.feature.student.reservation.domain.model.waitlist.WaitlistReservation

internal interface WaitlistReservationRepository {
    fun getWaitlistReservation(classId: String): WaitlistReservation
}
