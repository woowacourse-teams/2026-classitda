package com.classitda.feature.student.mypage.mypassdetail.model

import com.classitda.feature.student.mypage.mypass.model.MyPassStatus
import kotlinx.datetime.LocalDate

data class MyPassDetailUiModel(
    val status: MyPassStatus,
    val title: String,
    val studioName: String,
    val dDayLabel: String,
    val totalRemainingCount: Int,
    val reservableCount: Int,
    val cancellableCount: Int,
    val validPeriodLabel: String,
    val holdingDaysLabel: String,
    val currentExpireDate: LocalDate?,
    val historyEntries: List<MyPassHistoryEntryUiModel>,
)
