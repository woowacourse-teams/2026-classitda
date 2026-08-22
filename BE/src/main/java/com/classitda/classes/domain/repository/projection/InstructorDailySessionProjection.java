package com.classitda.classes.domain.repository.projection;

import com.classitda.classes.domain.ClassSession;

public interface InstructorDailySessionProjection {

    ClassSession getSession();

    String getInstructorName();

    Long getClassTypeId();

    String getClassTypeName();

    long getReservedCount();

    long getWaitingCount();
}
