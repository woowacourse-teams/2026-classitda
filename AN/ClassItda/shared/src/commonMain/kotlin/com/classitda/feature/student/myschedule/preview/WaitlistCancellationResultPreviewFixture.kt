package com.classitda.feature.student.myschedule.preview

import com.classitda.domain.model.student.myschedule.WaitlistId
import com.classitda.feature.student.myschedule.contract.WaitlistCancellationResultUiModel

internal object WaitlistCancellationResultPreviewFixture {
    val completed =
        WaitlistCancellationResultUiModel(
            waitlistId = WaitlistId("preview-waitlist-cancellation-result"),
            title = "리포머 밸런스",
            instructorName = "이지은 강사",
            dateLabel = "2026.08.06 (목)",
            timeRangeLabel = "오후 7:30 ~ 8:20",
            cancelledAtLabel = "2026.08.04 오후 2:32",
            positionAtCancellation = 2,
        )
}
