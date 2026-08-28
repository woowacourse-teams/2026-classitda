package com.classitda.classes.domain.repository.projection;

import com.classitda.classes.domain.enrollment.AttendanceResult;
import com.classitda.classes.domain.session.ClassSession;
import com.classitda.classes.domain.enrollment.EnrollmentStatus;
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
