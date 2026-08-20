package com.classitda.feature.student.mypage.holding.model

data class MyPassHoldingUiState(
    val passName: String,
    val startDateLabel: String,
    val endDateLabel: String,
    val memo: String,
    val totalHoldingDaysLabel: String,
    val currentExpireDateLabel: String,
    val newExpireDateLabel: String,
    val confirmDialogDescription: String? = null,
    val isSubmitting: Boolean = false,
)
