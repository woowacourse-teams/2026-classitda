package com.classitda.feature.student.reservation.domain.repository.classreservation

import com.classitda.feature.student.reservation.domain.model.classreservation.ClassReservation

internal interface ClassReservationRepository {
    fun getClassReservation(classId: String): ClassReservation
}
