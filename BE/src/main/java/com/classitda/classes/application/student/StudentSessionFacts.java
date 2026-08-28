package com.classitda.classes.application.student;

import com.classitda.classes.domain.session.BookingWindow;
import com.classitda.classes.domain.enrollment.AttendanceResult;
import com.classitda.classes.domain.enrollment.EnrollmentStatus;
import java.util.Optional;

public record StudentSessionFacts(
        BookingWindow bookingWindow,
        Optional<EnrollmentStatus> ownEnrollmentStatus,
        AttendanceResult attendanceResult,
        long remainingCapacity
) {
}
