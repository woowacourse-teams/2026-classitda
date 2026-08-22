package com.classitda.classes.application.student;

import com.classitda.classes.domain.BookingWindow;
import com.classitda.classes.domain.AttendanceResult;
import com.classitda.classes.domain.EnrollmentStatus;
import java.util.Optional;

public record StudentSessionFacts(
        BookingWindow bookingWindow,
        Optional<EnrollmentStatus> ownEnrollmentStatus,
        AttendanceResult attendanceResult,
        long remainingCapacity
) {
}
