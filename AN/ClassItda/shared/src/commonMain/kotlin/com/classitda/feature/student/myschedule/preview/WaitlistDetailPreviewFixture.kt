package com.classitda.feature.student.myschedule.preview

import com.classitda.domain.model.student.myschedule.WaitlistId
import com.classitda.feature.student.myschedule.contract.WaitlistCancellationAvailabilityUiModel
import com.classitda.feature.student.myschedule.contract.WaitlistClassInfoUiModel
import com.classitda.feature.student.myschedule.contract.WaitlistDetailUiModel
import com.classitda.feature.student.myschedule.contract.WaitlistPassAvailabilityUiModel

internal object WaitlistDetailPreviewFixture {
    val pending =
        WaitlistDetailUiModel(
            waitlistId = WaitlistId("preview-waitlist-pending"),
            title = "체어 밸런스",
            appliedAtLabel = "2026.08.01 (토) 오후 3:20",
            currentPosition = 2,
            classInfo =
                WaitlistClassInfoUiModel(
                    dateLabel = "2026.08.04 (화)",
                    timeRangeLabel = "오후 6:30 ~ 7:20",
                    memo = "오늘 평소보다 난이도가 조금 있는 수업입니다.",
                    instructorName = "박소연 강사",
                    facilityName = "클래스잇다 금토동지점",
                ),
            pass =
                WaitlistPassAvailabilityUiModel(
                    name = "[8:1] 그룹 레슨 20회권",
                    validityLabel = "2026.06.30 ~ 2026.08.20",
                    remainingUses = 14,
                    reservableUses = 5,
                    cancellableUses = 2,
                ),
            cancellation = WaitlistCancellationAvailabilityUiModel.Available,
        )
}
