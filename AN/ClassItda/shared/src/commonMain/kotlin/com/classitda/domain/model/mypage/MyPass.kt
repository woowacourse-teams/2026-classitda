package com.classitda.domain.model.mypage

import kotlinx.datetime.LocalDate

data class MyPass(
    val id: String,
    val name: String,
    val status: MyPassStatus,
    val totalRemainingCount: Int,
    val reservableCount: Int,
    val cancellableCount: Int,
    val validFrom: LocalDate? = null,
    val validUntil: LocalDate? = null,
    val holdingPeriod: HoldingPeriod? = null,
)

enum class MyPassStatus {
    IN_USE,
    EXPIRED,
    TERMINATED,
}

data class HoldingPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate,
)
