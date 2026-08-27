package com.classitda.feature.student.mypage.mypassdetail.model

data class MyPassHistoryEntryUiModel(
    val id: String,
    val title: String,
    val changeLabel: String,
    val changeType: MyPassHistoryChangeType,
    val description: String,
    val dateLabel: String,
)

enum class MyPassHistoryChangeType {
    DEDUCTION,
    RESTORATION,
    HOLD,
    ISSUANCE,
}
