package com.classitda.classes.domain.repository.projection;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassSessionStatus;
import java.time.LocalDateTime;

public interface ClassSessionDailyProjection {

    Long getClassSessionId();

    Long getInstructorMembershipId();

    String getInstructorName();

    ClassForm getClassForm();

    Long getClassTypeId();

    String getClassTypeName();

    String getClassName();

    String getDescription();

    int getCapacity();

    LocalDateTime getStartAt();

    LocalDateTime getEndAt();

    ClassSessionStatus getSessionStatus();
}
