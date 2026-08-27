package com.classitda.feature.student.myschedule.component.common

import com.classitda.feature.student.myschedule.contract.HistoryScheduleStatusUiModel

internal fun HistoryScheduleStatusUiModel.toScheduleStatusChipType(): ScheduleStatusChipType =
    when (this) {
        HistoryScheduleStatusUiModel.COMPLETED -> ScheduleStatusChipType.COMPLETED
        HistoryScheduleStatusUiModel.RESERVATION_CANCELED -> ScheduleStatusChipType.RESERVATION_CANCELED
    }
