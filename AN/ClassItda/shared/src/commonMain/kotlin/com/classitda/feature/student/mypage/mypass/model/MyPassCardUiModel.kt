package com.classitda.feature.student.mypage.mypass.model

data class MyPassCardUiModel(
    val id: String,
    val status: MyPassStatus,
    val periodLabel: String,
    val title: String,
    val totalRemainingCount: Int,
    val reservableCount: Int,
    val cancellableCount: Int,
    val holdingPeriod: String? = null,
)
