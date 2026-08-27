package com.classitda.domain.model.home

import kotlinx.datetime.LocalDate

data class Pass(
    val id: String,
    val name: String,
    val expireDate: LocalDate,
    val totalRemainingCount: Int,
    val reservableCount: Int,
    val cancellableCount: Int,
    val holdingPeriod: HoldingPeriod? = null,
)

data class HoldingPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate,
)
