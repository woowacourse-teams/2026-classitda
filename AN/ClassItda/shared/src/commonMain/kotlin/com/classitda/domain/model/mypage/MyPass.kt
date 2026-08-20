package com.classitda.domain.model.mypage

import kotlinx.datetime.LocalDate

data class MyPass(
    val id: String,
    val name: String,
    val studioName: String,
    val status: MyPassStatus,
    val totalRemainingCount: Int,
    val reservableCount: Int,
    val cancellableCount: Int,
    val validFrom: LocalDate? = null,
    val validUntil: LocalDate? = null,
    val holdingPeriod: HoldingPeriod? = null,
) {
    init {
        require((validFrom == null) == (validUntil == null)) {
            "수강권 유효 시작일과 종료일은 함께 존재하거나 함께 없어야 합니다."
        }
        require(validFrom == null || validFrom <= validUntil!!) {
            "수강권 유효 시작일은 종료일보다 늦을 수 없습니다."
        }
    }

    val isUnlimitedPeriod: Boolean get() = validFrom == null
}

enum class MyPassStatus {
    IN_USE,
    EXPIRED,
    TERMINATED,
}

data class HoldingPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate,
)
