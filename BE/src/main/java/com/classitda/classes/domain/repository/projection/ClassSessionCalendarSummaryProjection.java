package com.classitda.classes.domain.repository.projection;

import java.time.LocalDate;

public interface ClassSessionCalendarSummaryProjection {

    LocalDate getDate();

    int getScheduled();

    int getCompleted();

    int getMineScheduled();

    int getMineCompleted();
}
