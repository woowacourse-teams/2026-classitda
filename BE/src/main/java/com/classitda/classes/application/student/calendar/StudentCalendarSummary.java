package com.classitda.classes.application.student.calendar;

import java.time.LocalDate;

public record StudentCalendarSummary(
        LocalDate date,
        boolean pastReservation,
        boolean reserved,
        boolean waiting
) {

    public static StudentCalendarSummary of(
            LocalDate date,
            boolean pastReservation,
            boolean reserved,
            boolean waiting
    ) {
        return new StudentCalendarSummary(date, pastReservation, reserved, waiting);
    }

    StudentCalendarSummary merge(StudentCalendarSummary other) {
        return of(
                date,
                pastReservation || other.pastReservation,
                reserved || other.reserved,
                waiting || other.waiting
        );
    }
}
