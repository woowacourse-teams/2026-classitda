package com.classitda.feature.student.home.model
data class MyPassUiModel(
    val passName: String,
    val expireDateText: String,
    val totalRemaining: Int,
    val reservable: Int,
    val cancellable: Int,
    val holdingPeriodText: String? = null,
)
