package com.classitda.classes.domain.repository.projection;

public interface ReservationSummaryProjection {

    Long getClassSessionId();

    long getReservedCount();

    long getOwnReservedCount();

    long getOwnAttendedCount();

    long getOwnAbsentCount();
}
