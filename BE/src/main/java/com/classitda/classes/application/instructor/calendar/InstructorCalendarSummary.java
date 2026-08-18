package com.classitda.classes.application.instructor.calendar;

import com.classitda.classes.application.instructor.InstructorSessionStatus;
import java.time.LocalDate;

public record InstructorCalendarSummary(
        LocalDate date,
        long scheduledCount,
        long completedCount,
        long mineScheduledCount,
        long mineCompletedCount
) {

    static InstructorCalendarSummary empty(LocalDate date) {
        return new InstructorCalendarSummary(date, 0, 0, 0, 0);
    }

    InstructorCalendarSummary add(InstructorSessionStatus status, boolean mine) {
        return switch (status) {
            case SCHEDULED_OPEN, SCHEDULED_CLOSED -> new InstructorCalendarSummary(
                    date,
                    scheduledCount + 1,
                    completedCount,
                    mine ? mineScheduledCount + 1 : mineScheduledCount,
                    mineCompletedCount
            );
            case COMPLETED -> new InstructorCalendarSummary(
                    date,
                    scheduledCount,
                    completedCount + 1,
                    mineScheduledCount,
                    mine ? mineCompletedCount + 1 : mineCompletedCount
            );
            case IN_PROGRESS, CANCELED -> this;
        };
    }

    boolean isEmpty() {
        return scheduledCount == 0 && completedCount == 0;
    }
}
