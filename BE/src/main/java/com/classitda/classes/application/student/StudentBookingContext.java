package com.classitda.classes.application.student;

import com.classitda.classes.domain.ClassSessionStatus;
import java.time.LocalDateTime;

public record StudentBookingContext(
        ClassSessionStatus sessionStatus,
        LocalDateTime startAt,
        LocalDateTime endAt,
        long ownReservedCount,
        long ownOfferedCount,
        long ownWaitingCount,
        int reservationCloseMinutesBefore,
        long remainingCapacity,
        LocalDateTime now
) {
}
