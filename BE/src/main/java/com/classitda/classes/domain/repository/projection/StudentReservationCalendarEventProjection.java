package com.classitda.classes.domain.repository.projection;

import com.classitda.classes.domain.ReservationStatus;
import java.time.LocalDateTime;

public interface StudentReservationCalendarEventProjection {

    Long getClassSessionId();

    Long getClassTypeId();

    LocalDateTime getStartAt();

    LocalDateTime getEndAt();

    ReservationStatus getReservationStatus();
}
