package com.classitda.feature.student.myschedule.preview

import com.classitda.domain.model.student.myschedule.WaitlistId
import com.classitda.feature.student.myschedule.contract.WaitlistCancellationAvailabilityUiModel
import com.classitda.feature.student.myschedule.contract.WaitlistCancellationResultUiModel
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

    val approvalRequired =
        pending.copy(
            waitlistId = WaitlistId("preview-waitlist-approval-required"),
            currentPosition = 0,
        )

    object Boundary {
        private const val LONG_TITLE =
            "초보자부터 숙련자까지 함께하는 리포머 코어 밸런스 집중 수업"

        val pending =
            WaitlistDetailPreviewFixture.pending.copy(
                waitlistId = WaitlistId("preview-boundary-waitlist-pending"),
                title = LONG_TITLE,
                classInfo =
                    WaitlistDetailPreviewFixture.pending.classInfo.copy(
                        memo =
                            "오늘은 코어 안정화와 균형 동작을 함께 진행합니다. " +
                                "처음 참여하는 회원은 수업 전에 강사에게 알려주세요.",
                        instructorName = "아주 긴 이름을 가진 이지은 시니어 필라테스 대표 강사",
                        facilityName = "클래스잇다 서울 강남역 프리미엄 리포머 스튜디오 지점",
                    ),
                pass =
                    WaitlistDetailPreviewFixture.pending.pass.copy(
                        name = "평일 저녁 프리미엄 소그룹 리포머 필라테스 20회 수강권",
                    ),
            )

        val completed =
            WaitlistCancellationResultUiModel(
                waitlistId = WaitlistId("preview-boundary-waitlist-cancellation-result"),
                title = LONG_TITLE,
                instructorName = "아주 긴 이름을 가진 이지은 시니어 필라테스 대표 강사",
                dateLabel = "2026.08.06 (목)",
                timeRangeLabel = "오후 7:30 ~ 8:20",
                cancelledAtLabel = "2026.08.04 오후 2:32",
                positionAtCancellation = 2,
            )
    }
}
