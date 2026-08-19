package com.classitda.classes.domain.repository.projection;

import com.classitda.classes.domain.ClassForm;
import java.time.LocalDateTime;

public interface StudentWaitingCalendarEventProjection {

    Long getClassSessionId();

    ClassForm getClassForm();

    Long getClassTypeId();

    LocalDateTime getStartAt();

    LocalDateTime getEndAt();
}
