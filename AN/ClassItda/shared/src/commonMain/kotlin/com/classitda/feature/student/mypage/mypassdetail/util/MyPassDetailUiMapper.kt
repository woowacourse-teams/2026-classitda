package com.classitda.feature.student.mypage.mypassdetail.util

import com.classitda.domain.model.mypage.MyPass
import com.classitda.domain.model.mypage.MyPassHistoryEntry
import com.classitda.domain.model.mypage.MyPassHistoryEntryType
import com.classitda.feature.student.mypage.mypassdetail.model.MyPassDetailUiModel
import com.classitda.feature.student.mypage.mypassdetail.model.MyPassHistoryEntryUiModel
import com.classitda.feature.student.mypage.util.formatDateDot
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import com.classitda.domain.model.mypage.MyPassStatus as DomainMyPassStatus
import com.classitda.feature.student.mypage.mypass.model.MyPassStatus as UiMyPassStatus
import com.classitda.feature.student.mypage.mypassdetail.model.MyPassHistoryChangeType as UiMyPassHistoryChangeType

private const val MAX_HOLDING_DAYS_PER_YEAR = 30

internal fun MyPass.toDetailUiModel(
    history: List<MyPassHistoryEntry>,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): MyPassDetailUiModel {
    val today = Clock.System.todayIn(timeZone)
    val usedHoldingDays = history.filter { it.type == MyPassHistoryEntryType.HOLD }.sumOf { it.value }
    val remainingHoldingDays = (MAX_HOLDING_DAYS_PER_YEAR - usedHoldingDays).coerceAtLeast(0)

    return MyPassDetailUiModel(
        status = status.toUiModel(),
        title = name,
        studioName = studioName,
        dDayLabel = validUntil?.let { "D-${today.daysUntil(it)}" } ?: "기간 무제한",
        totalRemainingCount = totalRemainingCount,
        reservableCount = reservableCount,
        cancellableCount = cancellableCount,
        validPeriodLabel = periodLabel(),
        holdingDaysLabel = "총 ${MAX_HOLDING_DAYS_PER_YEAR}일 (잔여 ${remainingHoldingDays}일)",
        currentExpireDate = validUntil,
        historyEntries = history.map { it.toUiModel() },
    )
}

private fun MyPass.periodLabel(): String {
    val from = validFrom
    val until = validUntil
    return if (from == null || until == null) {
        "기간 무제한"
    } else {
        "${formatDateDot(from)} ~ ${formatDateDot(until)}"
    }
}

private fun MyPassHistoryEntry.toUiModel(): MyPassHistoryEntryUiModel =
    MyPassHistoryEntryUiModel(
        id = id,
        title = title,
        changeLabel = changeLabel(),
        changeType = type.toUiModel(),
        description = description,
        dateLabel = dateLabel(),
    )

private fun MyPassHistoryEntry.changeLabel(): String =
    when (type) {
        MyPassHistoryEntryType.DEDUCTION -> "-${value}회"
        MyPassHistoryEntryType.RESTORATION -> "+${value}회"
        MyPassHistoryEntryType.HOLD -> "${value}일 중지"
        MyPassHistoryEntryType.ISSUANCE -> "+${value}회"
    }

private fun MyPassHistoryEntry.dateLabel(): String {
    val until = occurredUntil
    return if (until != null) {
        "${formatDateDot(occurredAt.date)} ~ ${formatDateDot(until.date)}"
    } else {
        "${formatDateDot(occurredAt.date)} ${formatTime(occurredAt)}"
    }
}

private fun MyPassHistoryEntryType.toUiModel(): UiMyPassHistoryChangeType =
    when (this) {
        MyPassHistoryEntryType.DEDUCTION -> UiMyPassHistoryChangeType.DEDUCTION
        MyPassHistoryEntryType.RESTORATION -> UiMyPassHistoryChangeType.RESTORATION
        MyPassHistoryEntryType.HOLD -> UiMyPassHistoryChangeType.HOLD
        MyPassHistoryEntryType.ISSUANCE -> UiMyPassHistoryChangeType.ISSUANCE
    }

private fun DomainMyPassStatus.toUiModel(): UiMyPassStatus =
    when (this) {
        DomainMyPassStatus.IN_USE -> UiMyPassStatus.IN_USE
        DomainMyPassStatus.EXPIRED -> UiMyPassStatus.EXPIRED
        DomainMyPassStatus.TERMINATED -> UiMyPassStatus.TERMINATED
    }

private fun formatTime(dateTime: LocalDateTime): String =
    "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
