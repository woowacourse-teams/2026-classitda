package com.classitda.domain.model.student.myschedule

data class UsageHistoryEntry(
    val reservationId: ReservationId,
    val session: ClassSession,
    val status: UsageHistoryStatus,
)

enum class UsageHistoryStatus {
    ATTENDED,
    ABSENT,
    RESERVATION_CANCELLED,
}
