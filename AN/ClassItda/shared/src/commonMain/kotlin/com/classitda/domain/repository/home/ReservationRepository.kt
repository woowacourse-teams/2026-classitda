package com.classitda.domain.repository.home

import com.classitda.domain.model.home.PendingReservation
import com.classitda.domain.model.home.UpcomingReservation

interface ReservationRepository {
    suspend fun getPendingReservation(): PendingReservation?

    suspend fun getNextUpcomingReservation(): UpcomingReservation?

    suspend fun approveReservation(reservationId: String)
}
