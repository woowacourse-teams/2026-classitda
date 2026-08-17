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
}
