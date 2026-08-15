package com.classitda.domain.repository.classreservation

import com.classitda.domain.model.classreservation.ClassReservation

internal interface ClassReservationRepository {
    fun getClassReservation(classId: String): ClassReservation
}
