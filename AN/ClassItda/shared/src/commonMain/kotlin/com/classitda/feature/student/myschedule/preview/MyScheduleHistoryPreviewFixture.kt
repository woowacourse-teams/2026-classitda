package com.classitda.feature.student.myschedule.preview

import androidx.compose.runtime.Composable
import com.classitda.feature.student.myschedule.contract.HistoryScheduleItemUiModel
import com.classitda.feature.student.myschedule.contract.HistoryScheduleStatusUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleItemId
import com.classitda.feature.student.myschedule.utils.previewHistoryScheduleDateTime
import kotlin.time.Instant

@Composable
internal fun myScheduleHistoryPreviewItems(): List<HistoryScheduleItemUiModel> =
    listOf(
        HistoryScheduleItemUiModel(
            id = ScheduleItemId("preview-history-chair-balance"),
            title = "체어 밸런스",
            dateTime =
                previewHistoryScheduleDateTime(
                    startAt = Instant.parse("2026-08-04T09:30:00Z"),
                    endAt = Instant.parse("2026-08-04T10:20:00Z"),
                ),
            locationLabel = "스튜디오 A",
            instructorName = "이지은",
            status = HistoryScheduleStatusUiModel.COMPLETED,
        ),
        HistoryScheduleItemUiModel(
            id = ScheduleItemId("preview-history-reformer-basic"),
            title = "리포머 베이직",
            dateTime =
                previewHistoryScheduleDateTime(
                    startAt = Instant.parse("2026-08-01T01:00:00Z"),
                    endAt = Instant.parse("2026-08-01T01:50:00Z"),
                ),
            locationLabel = "리포머룸",
            instructorName = "이지은",
            status = HistoryScheduleStatusUiModel.COMPLETED,
        ),
        HistoryScheduleItemUiModel(
            id = ScheduleItemId("preview-history-barrel-core-therapy"),
            title = "바렐 코어 테라피",
            dateTime =
                previewHistoryScheduleDateTime(
                    startAt = Instant.parse("2026-07-29T10:30:00Z"),
                    endAt = Instant.parse("2026-07-29T11:20:00Z"),
                ),
            locationLabel = "체어룸",
            instructorName = "이지은",
            status = HistoryScheduleStatusUiModel.COMPLETED,
        ),
        HistoryScheduleItemUiModel(
            id = ScheduleItemId("preview-history-cadillac-stretching-canceled"),
            title = "캐딜락 스트레칭",
            dateTime =
                previewHistoryScheduleDateTime(
                    startAt = Instant.parse("2026-07-25T02:00:00Z"),
                    endAt = Instant.parse("2026-07-25T02:50:00Z"),
                ),
            locationLabel = "하타룸",
            instructorName = "박소연",
            status = HistoryScheduleStatusUiModel.RESERVATION_CANCELED,
        ),
    )
