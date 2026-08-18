package com.classitda.classes.domain.repository.projection;

import java.time.LocalDate;

public interface StudentCalendarSummaryProjection {

    LocalDate getDate();

    int getAttended();

    int getReserved();

    int getWaiting();
}
