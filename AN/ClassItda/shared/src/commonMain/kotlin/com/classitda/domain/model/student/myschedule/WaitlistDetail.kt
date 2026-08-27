package com.classitda.domain.model.student.myschedule

import kotlin.time.Instant

data class WaitlistDetail(
    val waitlistId: WaitlistId,
    val session: ClassSession,
    val appliedAt: Instant,
    val currentPosition: Int,
    val pass: MemberPassAvailability,
    val cancellation: WaitlistCancellationAvailability,
) {
    init {
        require(currentPosition >= 0) { "현재 대기 순번은 0 이상이어야 합니다." }
    }
}
