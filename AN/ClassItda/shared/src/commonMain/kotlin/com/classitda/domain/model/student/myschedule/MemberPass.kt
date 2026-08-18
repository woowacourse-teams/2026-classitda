package com.classitda.domain.model.student.myschedule

import kotlinx.datetime.LocalDate

data class MemberPassSummary(
    val id: MemberPassId,
    val name: String,
    val validFrom: LocalDate,
    val validUntil: LocalDate,
) {
    init {
        require(name.isNotBlank()) { "수강권 이름은 비어 있을 수 없습니다." }
        require(validFrom <= validUntil) { "수강권 유효 시작일은 종료일보다 늦을 수 없습니다." }
    }
}

data class MemberPassAvailability(
    val pass: MemberPassSummary,
    val remainingUses: Int,
    val reservableUses: Int,
    val cancellableUses: Int,
) {
    init {
        require(remainingUses >= 0) { "수강권 잔여 횟수는 음수일 수 없습니다." }
        require(reservableUses >= 0) { "수강권 예약 가능 횟수는 음수일 수 없습니다." }
        require(cancellableUses >= 0) { "수강권 취소 가능 횟수는 음수일 수 없습니다." }
    }
}
