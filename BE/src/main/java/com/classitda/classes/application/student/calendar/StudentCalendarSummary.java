package com.classitda.classes.application.student.calendar;

import java.time.LocalDate;

public record StudentCalendarSummary(
        LocalDate date,
        boolean attended,
        boolean reserved,
        boolean waiting
) {
}
