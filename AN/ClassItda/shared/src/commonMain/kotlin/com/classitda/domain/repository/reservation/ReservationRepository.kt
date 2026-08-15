package com.classitda.domain.repository.reservation

import com.classitda.domain.model.reservation.ReservationClass

internal interface ReservationRepository {
    fun getClasses(): List<ReservationClass>
}
