package com.classitda.classes.application.student;

import com.classitda.classes.domain.AttendanceResult;

public record StudentBookingDecision(
        StudentBookingRelation bookingRelation,
        AttendanceResult attendanceResult,
        BookingAvailability availability
) {
}
