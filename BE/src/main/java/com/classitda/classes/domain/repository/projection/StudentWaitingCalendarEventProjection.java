package com.classitda.classes.domain.repository.projection;

import java.time.LocalDateTime;

public interface StudentWaitingCalendarEventProjection {

    Long getClassSessionId();

    Long getClassTypeId();

    LocalDateTime getStartAt();

    LocalDateTime getEndAt();
}
