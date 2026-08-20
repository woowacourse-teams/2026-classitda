package com.classitda.feature.student.mypage.mypass.util

import com.classitda.domain.model.mypage.MyPass
import com.classitda.feature.student.mypage.mypass.model.MyPassCardUiModel
import com.classitda.feature.student.mypage.util.formatDateDot
import com.classitda.domain.model.mypage.MyPassStatus as DomainMyPassStatus
import com.classitda.feature.student.mypage.mypass.model.MyPassStatus as UiMyPassStatus

fun MyPass.toUiModel(): MyPassCardUiModel =
    MyPassCardUiModel(
        id = id,
        status = status.toUiModel(),
        periodLabel = periodLabel(),
        title = name,
        totalRemainingCount = totalRemainingCount,
        reservableCount = reservableCount,
        cancellableCount = cancellableCount,
        holdingPeriod = holdingPeriod?.let { "${formatDateDot(it.startDate)} ~ ${formatDateDot(it.endDate)}" },
    )

private fun MyPass.periodLabel(): String {
    val from = validFrom
    val until = validUntil
    return if (from == null || until == null) {
        "기간 무제한"
    } else {
        "${formatDateDot(from)} ~ ${formatDateDot(until)}"
    }
}

private fun DomainMyPassStatus.toUiModel(): UiMyPassStatus =
    when (this) {
        DomainMyPassStatus.IN_USE -> UiMyPassStatus.IN_USE
        DomainMyPassStatus.EXPIRED -> UiMyPassStatus.EXPIRED
        DomainMyPassStatus.TERMINATED -> UiMyPassStatus.TERMINATED
    }
