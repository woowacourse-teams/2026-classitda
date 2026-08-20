package com.classitda.domain.model.mypage

import kotlinx.datetime.LocalDate

data class MyPassHoldingReceipt(
    val requestedFrom: LocalDate,
    val requestedUntil: LocalDate,
    val totalHoldingDays: Int,
    val previousExpireDate: LocalDate,
    val newExpireDate: LocalDate,
)
