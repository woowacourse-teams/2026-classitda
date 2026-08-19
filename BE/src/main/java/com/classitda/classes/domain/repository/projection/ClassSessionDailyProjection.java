package com.classitda.classes.domain.repository.projection;

import com.classitda.classes.domain.ClassSession;

public interface ClassSessionDailyProjection {

    ClassSession getSession();

    Long getInstructorMembershipId();

    String getInstructorName();

    Long getClassTypeId();

    String getClassTypeName();
}
