package com.classitda.feature.student.mypage.holding.util

import com.classitda.domain.model.mypage.MyPassHoldingReceipt
import com.classitda.feature.student.mypage.holding.model.MyPassHoldingCompletedUiModel
import com.classitda.feature.student.mypage.util.formatDateDot
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

internal fun MyPassHoldingReceipt.toCompletedUiModel(): MyPassHoldingCompletedUiModel =
    MyPassHoldingCompletedUiModel(
        requestPeriodLabel = "${formatDateWithWeekday(requestedFrom)} ~ ${formatDateWithWeekday(requestedUntil)}",
        totalHoldingDaysLabel = "${totalHoldingDays}일",
        currentExpireDateLabel = formatDateWithWeekday(previousExpireDate),
        newExpireDateLabel = formatDateWithWeekday(newExpireDate),
    )

internal fun formatDateWithWeekday(date: LocalDate): String =
    "${formatDateDot(date)}(${date.dayOfWeek.toKoreanShort()})"

internal fun formatFullKoreanDate(date: LocalDate): String = "${date.year}년 ${date.month.number}월 ${date.day}일"

internal fun formatMonthDayKorean(date: LocalDate): String = "${date.month.number}월 ${date.day}일"

private fun DayOfWeek.toKoreanShort(): String =
    when (this) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }
