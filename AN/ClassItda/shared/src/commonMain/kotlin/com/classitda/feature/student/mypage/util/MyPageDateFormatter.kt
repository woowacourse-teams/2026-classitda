package com.classitda.feature.student.mypage.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

internal fun formatDateDot(date: LocalDate): String =
    "${date.year}.${date.month.number.toString().padStart(2, '0')}.${date.day.toString().padStart(2, '0')}"
