package com.classitda.classes.application.student;

import com.classitda.classes.domain.AttendanceResult;
import java.util.Optional;

public record StudentBookingDecision(
        StudentBookingRelation bookingRelation,
        AttendanceResult attendanceResult,
        Optional<BookingAvailability> availability
) {
}
