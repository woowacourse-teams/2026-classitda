package com.classitda.classes.application.student;

import com.classitda.classes.domain.BookingWindow;
import java.time.LocalDateTime;

public record StudentBookingContext(
        BookingWindow bookingWindow,
        LocalDateTime startAt,
        ReservationCounts reservation,
        WaitingCounts waiting,
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
