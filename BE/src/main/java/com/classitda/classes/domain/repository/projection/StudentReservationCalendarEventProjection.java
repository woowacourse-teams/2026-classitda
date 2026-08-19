package com.classitda.classes.domain.repository.projection;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ReservationStatus;
import java.time.LocalDateTime;

public interface StudentReservationCalendarEventProjection {

    Long getClassSessionId();

    ClassForm getClassForm();

    Long getClassTypeId();

    LocalDateTime getStartAt();

    LocalDateTime getEndAt();

    ReservationStatus getReservationStatus();
}
