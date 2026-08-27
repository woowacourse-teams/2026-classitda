package com.classitda.feature.student.myschedule.contract

data class UpcomingScheduleDateTimeUiModel(
    val sectionDateLabel: String,
    val timeRangeLabel: String,
)

data class HistoryScheduleDateTimeUiModel(
    val monthLabel: String,
    val dateTimeLabel: String,
)

data class ReservationDetailDateTimeUiModel(
    val dateLabel: String,
    val timeRangeLabel: String,
)
