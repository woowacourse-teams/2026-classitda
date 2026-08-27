package com.classitda.domain.model.mypage

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

object MyPassHoldingCalculator {
    fun calculate(
        startDate: LocalDate,
        endDate: LocalDate,
        previousExpireDate: LocalDate,
    ): MyPassHoldingReceipt {
        val totalHoldingDays = startDate.daysUntil(endDate) + 1
        return MyPassHoldingReceipt(
            requestedFrom = startDate,
            requestedUntil = endDate,
            totalHoldingDays = totalHoldingDays,
            previousExpireDate = previousExpireDate,
            newExpireDate = previousExpireDate.plus(totalHoldingDays, DateTimeUnit.DAY),
        )
    }
}
