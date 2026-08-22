package com.classitda.classes.domain.repository.projection;

import com.classitda.classes.domain.AttendanceResult;
import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.EnrollmentStatus;
import java.util.Optional;

public interface StudentDailySessionProjection {

    ClassSession getSession();

    String getInstructorName();

    Long getClassTypeId();

    String getClassTypeName();

    long getReservedCount();

    long getWaitingCount();

    Optional<Long> getOwnEnrollmentId();

    Optional<EnrollmentStatus> getOwnEnrollmentStatus();

    Optional<AttendanceResult> getOwnAttendanceResult();
}
