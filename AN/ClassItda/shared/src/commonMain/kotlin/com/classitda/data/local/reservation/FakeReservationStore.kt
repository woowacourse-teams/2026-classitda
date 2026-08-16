package com.classitda.data.local.reservation

import com.classitda.domain.model.reservation.ReservationClass

internal class FakeReservationStore {
    private val classes =
        mutableListOf(
            ReservationClass(
                id = "1",
                classTime = "오전 10:00 - 10:50",
                className = "리포머 베이직",
                instructorName = "이지은 강사",
                roomName = "리포머룸",
                leftStudentCount = 4,
            ),
            ReservationClass(
                id = "2",
                classTime = "오후 2:00 - 2:50",
                className = "체어 밸런스",
                instructorName = "박소연 강사",
                roomName = "스튜디오 A",
                leftStudentCount = 0,
            ),
            ReservationClass(
                id = "3",
                classTime = "오후 7:30 - 8:20",
                className = "리포머 밸런스",
                instructorName = "이지은 강사",
                roomName = "스튜디오 B",
                leftStudentCount = 0,
                isReserved = true,
            ),
            ReservationClass(
                id = "4",
                classTime = "오후 9:30 - 10:20",
                className = "체어 베이직",
                instructorName = "박소연 강사",
                roomName = "바렐룸",
                leftStudentCount = 0,
                isWaitlisted = true,
            ),
        )

    fun getClasses(): List<ReservationClass> = classes.toList()

    fun saveReservation(classId: String) {
        updateClass(classId) { it.copy(isReserved = true, isWaitlisted = false) }
    }

    fun saveWaitlist(classId: String) {
        updateClass(classId) { it.copy(isReserved = false, isWaitlisted = true) }
    }

    private fun updateClass(
        classId: String,
        transform: (ReservationClass) -> ReservationClass,
    ) {
        val index = classes.indexOfFirst { it.id == classId }
        if (index >= 0) classes[index] = transform(classes[index])
    }
}
