package com.classitda.classes.application.instructor.calendar;

import java.time.LocalDate;

public record InstructorCalendarSummary(
        LocalDate date,
        boolean scheduled,
        boolean completed,
        boolean mineScheduled,
        boolean mineCompleted
) {
}
