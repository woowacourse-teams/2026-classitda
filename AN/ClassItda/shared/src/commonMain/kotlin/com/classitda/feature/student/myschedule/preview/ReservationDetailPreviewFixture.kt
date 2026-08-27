package com.classitda.feature.student.myschedule.preview

import com.classitda.domain.model.student.myschedule.ReservationId
import com.classitda.feature.student.myschedule.contract.ReservationCancellationAvailabilityUiModel
import com.classitda.feature.student.myschedule.contract.ReservationCancellationResultUiModel
import com.classitda.feature.student.myschedule.contract.ReservationClassInfoUiModel
import com.classitda.feature.student.myschedule.contract.ReservationDetailUiModel
import com.classitda.feature.student.myschedule.contract.ReservationPassAvailabilityUiModel
import com.classitda.feature.student.myschedule.contract.ReservationUsedPassUiModel

internal object ReservationDetailPreviewFixture {
    private val confirmedClassInfo =
        ReservationClassInfoUiModel(
            dateLabel = "2026.08.04 (화)",
            timeRangeLabel = "오후 6:30 ~ 7:20",
            memo = "오늘 평소보다 난이도가 조금 있는 수업입니다. 따라서 초보",
            instructorName = "박소연 강사",
            facilityName = "클래스잇다 금토동지점",
        )

    private val attendedClassInfo =
        confirmedClassInfo.copy(
            dateLabel = "2026년 8월 4일 화요일",
            memo = null,
        )

    private val absentClassInfo =
        confirmedClassInfo.copy(
            dateLabel = "2026.08.04 (화)",
            memo = null,
        )

    private val usedPass =
        ReservationUsedPassUiModel(
            name = "[8:1] 그룹 레슨 20회권",
            validityLabel = "2026.06.30 ~ 2026.08.20",
        )

    val confirmed =
        ReservationDetailUiModel.Confirmed(
            reservationId = ReservationId("preview-reservation-confirmed"),
            title = "체어 밸런스",
            classInfo = confirmedClassInfo,
            reservedAtLabel = "2026.08.01 (토) 오후 3:20",
            pass =
                ReservationPassAvailabilityUiModel(
                    name = "[8:1] 그룹 레슨 20회권",
                    validityLabel = "2026.06.30 ~ 2026.08.20",
                    remainingUses = 14,
                    reservableUses = 5,
                    cancellableUses = 2,
                ),
            cancellationDeadlineHoursBeforeStart = 4,
            cancellation =
                ReservationCancellationAvailabilityUiModel.Available(
                    hoursUntilStart = 22,
                    restoredPassUses = 1,
                ),
        )

    val cancelled =
        ReservationDetailUiModel.Cancelled(
            reservationId = ReservationId("preview-reservation-cancelled"),
            title = "체어 밸런스",
            classInfo = confirmedClassInfo,
            cancelledAtLabel = "2026.08.01 (토) 오후 3:25",
        )

    val classCancelled =
        ReservationDetailUiModel.ClassCancelled(
            reservationId = ReservationId("preview-reservation-class-cancelled"),
            title = "체어 밸런스",
            classInfo = confirmedClassInfo,
            cancelledAtLabel = "2026.08.01 (토) 오후 3:25",
        )

    val cancellationCompleted =
        ReservationCancellationResultUiModel(
            reservationId = ReservationId("preview-reservation-cancellation-result"),
            title = "리포머 밸런스",
            classInfo =
                ReservationClassInfoUiModel(
                    dateLabel = "2026.08.06 (목)",
                    timeRangeLabel = "오후 7:30 ~ 8:20",
                    memo = null,
                    instructorName = "이지은 강사",
                    facilityName = "클래스잇다 금토동지점",
                ),
            cancelledAtLabel = "2026.08.04 (화) 오후 2:32",
            restoredPassUses = 1,
        )

    val attended =
        ReservationDetailUiModel.Attended(
            reservationId = ReservationId("preview-reservation-attended"),
            title = "체어 밸런스",
            classInfo = attendedClassInfo,
            checkedInAtLabel = "2026.08.04(화) 오후 6:20",
            usedPass = usedPass,
        )

    val absent =
        ReservationDetailUiModel.Absent(
            reservationId = ReservationId("preview-reservation-absent"),
            title = "체어 밸런스",
            classInfo = absentClassInfo,
            attendanceTimePlaceholder = "--:--:--",
            usedPass = usedPass,
        )

    object Boundary {
        private const val LONG_TITLE =
            "초보자부터 숙련자까지 함께하는 리포머 코어 밸런스 집중 수업"

        private val longClassInfo =
            ReservationClassInfoUiModel(
                dateLabel = "2026.08.04 (화)",
                timeRangeLabel = "오후 6:30 ~ 7:20",
                memo =
                    "오늘은 코어 안정화와 균형 동작을 함께 진행합니다. " +
                        "처음 참여하는 회원은 수업 전에 강사에게 알려주세요.",
                instructorName = "아주 긴 이름을 가진 이지은 시니어 필라테스 대표 강사",
                facilityName = "클래스잇다 서울 강남역 프리미엄 리포머 스튜디오 지점",
            )

        val confirmed =
            ReservationDetailPreviewFixture.confirmed.copy(
                reservationId = ReservationId("preview-boundary-reservation-confirmed"),
                title = LONG_TITLE,
                classInfo = longClassInfo,
            )

        val cancelled =
            ReservationDetailPreviewFixture.cancelled.copy(
                reservationId = ReservationId("preview-boundary-reservation-cancelled"),
                title = LONG_TITLE,
                classInfo = longClassInfo,
            )

        val attended =
            ReservationDetailPreviewFixture.attended.copy(
                reservationId = ReservationId("preview-boundary-reservation-attended"),
                title = LONG_TITLE,
                classInfo =
                    longClassInfo.copy(
                        dateLabel = "2026년 8월 4일 화요일",
                        memo = null,
                    ),
            )

        val absent =
            ReservationDetailPreviewFixture.absent.copy(
                reservationId = ReservationId("preview-boundary-reservation-absent"),
                title = LONG_TITLE,
                classInfo = longClassInfo.copy(memo = null),
            )

        val cancellationCompleted =
            ReservationDetailPreviewFixture.cancellationCompleted.copy(
                reservationId = ReservationId("preview-boundary-cancellation-result"),
                title = LONG_TITLE,
                classInfo = longClassInfo.copy(memo = null),
            )
    }
}
