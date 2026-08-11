package com.classitda.feature.student.myschedule.preview

import androidx.compose.runtime.Composable
import com.classitda.feature.student.myschedule.contract.ScheduleCancellationAvailabilityUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleCancellationPolicyUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleItemId
import com.classitda.feature.student.myschedule.contract.ScheduleWaitlistReapplicationUiModel
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleItemUiModel
import com.classitda.feature.student.myschedule.utils.previewReservationDetailDateTime
import com.classitda.feature.student.myschedule.utils.previewUpcomingScheduleDateTime
import kotlin.time.Instant

internal data class WaitlistDetailPreviewFixture(
    val item: UpcomingScheduleItemUiModel.Waitlist,
    val dateLabel: String,
    val timeRangeLabel: String,
    val durationMinutes: Int,
    val deadlineHours: Int,
)

@Composable
internal fun waitlistDetailPreviewFixture(): WaitlistDetailPreviewFixture {
    val startAt = Instant.parse("2026-08-09T02:00:00Z")
    val endAt = Instant.parse("2026-08-09T02:50:00Z")
    val detailDateTime = previewReservationDetailDateTime(startAt = startAt, endAt = endAt)

    return WaitlistDetailPreviewFixture(
        item =
            UpcomingScheduleItemUiModel.Waitlist(
                id = ScheduleItemId("preview-waitlist-detail"),
                title = "캐딜락 스트레칭",
                dateTime = previewUpcomingScheduleDateTime(startAt = startAt, endAt = endAt),
                locationLabel = "A 스튜디오 (2층)",
                instructorName = "박소연",
                position = 1,
                cancellation =
                    ScheduleCancellationAvailabilityUiModel.Available(
                        policy =
                            ScheduleCancellationPolicyUiModel.Waitlist(
                                reapplicationRule = ScheduleWaitlistReapplicationUiModel.LastPosition,
                            ),
                    ),
            ),
        dateLabel = detailDateTime.dateLabel,
        timeRangeLabel = detailDateTime.timeRangeLabel,
        durationMinutes = 50,
        deadlineHours = 4,
    )
}
