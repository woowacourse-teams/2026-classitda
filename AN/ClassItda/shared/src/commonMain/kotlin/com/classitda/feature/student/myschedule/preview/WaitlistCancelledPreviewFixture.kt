package com.classitda.feature.student.myschedule.preview

import androidx.compose.runtime.Composable
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_cancelled_at_label
import com.classitda.feature.student.myschedule.contract.ScheduleCancellationAvailabilityUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleCancellationPolicyUiModel
import com.classitda.feature.student.myschedule.contract.ScheduleItemId
import com.classitda.feature.student.myschedule.contract.ScheduleWaitlistReapplicationUiModel
import com.classitda.feature.student.myschedule.contract.UpcomingScheduleItemUiModel
import com.classitda.feature.student.myschedule.utils.previewReservationDetailDateTime
import com.classitda.feature.student.myschedule.utils.previewUpcomingScheduleDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

internal data class WaitlistCancelledPreviewFixture(
    val item: UpcomingScheduleItemUiModel.Waitlist,
    val dateLabel: String,
    val timeRangeLabel: String,
    val cancelledAtLabel: String,
)

@Composable
internal fun waitlistCancelledPreviewFixture(): WaitlistCancelledPreviewFixture {
    val startAt = Instant.parse("2026-07-25T02:00:00Z")
    val endAt = Instant.parse("2026-07-25T02:50:00Z")
    val detailDateTime = previewReservationDetailDateTime(startAt = startAt, endAt = endAt)

    return WaitlistCancelledPreviewFixture(
        item =
            UpcomingScheduleItemUiModel.Waitlist(
                id = ScheduleItemId("preview-waitlist-cancelled"),
                title = "캐딜락 스트레칭",
                dateTime = previewUpcomingScheduleDateTime(startAt = startAt, endAt = endAt),
                locationLabel = "A 스튜디오 (3번 룸)",
                instructorName = "박소연",
                position = 2,
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
        cancelledAtLabel =
            stringResource(
                Res.string.my_schedule_cancelled_at_label,
                2026,
                "07",
                "24",
                "16:15",
            ),
    )
}
