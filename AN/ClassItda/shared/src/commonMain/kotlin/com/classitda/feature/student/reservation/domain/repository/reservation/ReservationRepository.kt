package com.classitda.feature.student.reservation.domain.repository.reservation

import com.classitda.feature.student.reservation.domain.model.reservation.ReservationClass

internal interface ReservationRepository {
    fun getClasses(): List<ReservationClass>
}
