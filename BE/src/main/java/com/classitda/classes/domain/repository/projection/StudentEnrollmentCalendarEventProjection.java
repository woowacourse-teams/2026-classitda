package com.classitda.classes.domain.repository.projection;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.EnrollmentStatus;
import java.time.LocalDateTime;

public interface StudentEnrollmentCalendarEventProjection {

    ClassForm getClassForm();

    Long getClassTypeId();

    LocalDateTime getStartAt();

    EnrollmentStatus getEnrollmentStatus();
}
