package com.classitda.classes.application.student.calendar;

import java.time.LocalDate;

public record StudentCalendarSummary(
        LocalDate date,
        boolean pastReservation,
        boolean reserved,
        boolean waiting
) {
}
