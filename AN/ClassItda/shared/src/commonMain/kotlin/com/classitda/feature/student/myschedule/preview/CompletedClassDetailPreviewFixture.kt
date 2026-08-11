package com.classitda.feature.student.myschedule.preview

import androidx.compose.runtime.Composable
import com.classitda.feature.student.myschedule.contract.CompletedClassDetailUiModel
import com.classitda.feature.student.myschedule.contract.CompletedClassInstructorUiModel
import com.classitda.feature.student.myschedule.contract.CompletedClassLocationUiModel
import com.classitda.feature.student.myschedule.contract.CompletedClassTicketUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleItemId
import com.classitda.feature.student.myschedule.utils.previewReservationDetailDateTime
import kotlin.time.Instant

@Composable
internal fun completedClassDetailPreviewModel(): CompletedClassDetailUiModel =
    CompletedClassDetailUiModel(
        id = ScheduleItemId("preview-completed-class-detail"),
        title = "체어 밸런스",
        instructor =
            CompletedClassInstructorUiModel(
                name = "박소연",
                specialtyLabel = "재활 필라테스 전문가 · 체형 교정 전문",
            ),
        dateTime =
            previewReservationDetailDateTime(
                startAt = Instant.parse("2026-08-04T09:30:00Z"),
                endAt = Instant.parse("2026-08-04T10:20:00Z"),
            ),
        durationMinutes = 50,
        location =
            CompletedClassLocationUiModel(
                name = "필라테스 에이 스튜디오",
                detail = "2번 룸 (체어 전용)",
            ),
        ticket =
            CompletedClassTicketUiModel(
                name = "[8:1] 그룹 레슨 20회권",
                remainingCount = 14,
                totalCount = 20,
            ),
    )
