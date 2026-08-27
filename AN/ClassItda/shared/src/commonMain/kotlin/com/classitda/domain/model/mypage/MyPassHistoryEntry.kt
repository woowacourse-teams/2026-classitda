package com.classitda.domain.model.mypage

import kotlinx.datetime.LocalDateTime

data class MyPassHistoryEntry(
    val id: String,
    val type: MyPassHistoryEntryType,
    val title: String,
    val value: Int,
    val description: String,
    val occurredAt: LocalDateTime,
    val occurredUntil: LocalDateTime? = null,
)

enum class MyPassHistoryEntryType {
    DEDUCTION,
    RESTORATION,
    HOLD,
    ISSUANCE,
}
