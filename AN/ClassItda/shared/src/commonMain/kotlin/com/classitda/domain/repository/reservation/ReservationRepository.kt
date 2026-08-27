package com.classitda.domain.repository.reservation

import com.classitda.domain.model.reservation.ReservationClass
import com.classitda.domain.model.reservation.ReservationPass

internal interface ReservationRepository {
    fun getClasses(): List<ReservationClass>

    fun getPasses(): List<ReservationPass>
}
