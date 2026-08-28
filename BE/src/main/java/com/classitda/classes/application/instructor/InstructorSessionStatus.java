package com.classitda.classes.application.instructor;

import com.classitda.classes.domain.session.BookingWindow;
import com.classitda.classes.domain.session.SessionPhase;

public enum InstructorSessionStatus {
    SCHEDULED_BOOKING_OPEN,
    SCHEDULED_BOOKING_CLOSED,
    IN_PROGRESS,
    COMPLETED,
    CANCELED;

    public static InstructorSessionStatus from(SessionPhase phase, BookingWindow bookingWindow) {
        return switch (phase) {
            case SCHEDULED -> bookingWindow == BookingWindow.OPEN ? SCHEDULED_BOOKING_OPEN : SCHEDULED_BOOKING_CLOSED;
            case IN_PROGRESS -> IN_PROGRESS;
            case COMPLETED -> COMPLETED;
            case CANCELED -> CANCELED;
        };
    }
}
