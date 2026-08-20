package com.classitda.feature.student.mypage.holding

import kotlinx.datetime.LocalDate

internal data class MyPassHoldingRequestArgs(
    val passId: String,
    val passName: String,
    val currentExpireDate: LocalDate,
)
