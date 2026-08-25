package com.classitda.classes.domain.repository.projection;

import com.classitda.classes.domain.session.ClassSession;

public interface InstructorSessionDetailProjection {

    ClassSession getSession();

    Long getInstructorMembershipId();

    String getInstructorName();

    Long getClassTypeId();

    String getClassTypeName();
}
