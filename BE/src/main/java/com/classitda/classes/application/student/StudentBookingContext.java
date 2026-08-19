package com.classitda.classes.application.student;

import com.classitda.classes.domain.ClassSessionStatus;
import java.time.LocalDateTime;

public record StudentBookingContext(
        ClassSessionStatus sessionStatus,
        LocalDateTime startAt,
        ReservationCounts reservation,
        WaitingCounts waiting,
        int reservationCloseMinutesBefore,
        long remainingCapacity,
        LocalDateTime now
) {

    public record ReservationCounts(
            long totalCount,
            long ownReservedCount,
            long ownAttendedCount,
            long ownAbsentCount
    ) {
    }

    public record WaitingCounts(
            long totalCount,
            long ownOfferedCount,
            long ownWaitingCount
    ) {
    }
}
