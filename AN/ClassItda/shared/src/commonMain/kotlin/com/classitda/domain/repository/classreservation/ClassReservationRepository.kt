package com.classitda.domain.repository.classreservation

import com.classitda.domain.model.classreservation.ClassReservation
import com.classitda.domain.model.classreservation.ReservationRequestResult

internal interface ClassReservationRepository {
    fun getClassReservation(classId: String): ClassReservation

    fun reserve(
        classId: String,
        passId: String,
    ): ReservationRequestResult
}
