package com.classitda.classes.application.student;

import com.classitda.classes.domain.BookingWindow;
import com.classitda.classes.domain.ReservationStatus;
import com.classitda.classes.domain.WaitingStatus;
import java.time.LocalDateTime;
import java.util.Optional;

public record StudentSessionFacts(
        BookingWindow bookingWindow,
        LocalDateTime startAt,
        Optional<ReservationStatus> ownReservationStatus,
        Optional<WaitingStatus> ownWaitingStatus,
        long remainingCapacity,
        LocalDateTime now
) {
}
