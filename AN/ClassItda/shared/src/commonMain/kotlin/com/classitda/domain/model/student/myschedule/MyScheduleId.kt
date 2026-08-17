package com.classitda.domain.model.student.myschedule

import kotlin.jvm.JvmInline

@JvmInline
value class ClassSessionId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "수업 회차 ID는 비어 있을 수 없습니다." }
    }
}

@JvmInline
value class ReservationId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "예약 ID는 비어 있을 수 없습니다." }
    }
}

@JvmInline
value class WaitlistId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "대기 ID는 비어 있을 수 없습니다." }
    }
}

@JvmInline
value class InstructorId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "강사 ID는 비어 있을 수 없습니다." }
    }
}

@JvmInline
value class FacilityId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "시설 ID는 비어 있을 수 없습니다." }
    }
}

@JvmInline
value class MemberPassId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "보유 수강권 ID는 비어 있을 수 없습니다." }
    }
}
