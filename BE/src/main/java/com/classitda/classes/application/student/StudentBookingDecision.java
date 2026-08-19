package com.classitda.classes.application.student;

public record StudentBookingDecision(
        StudentBookingRelation bookingRelation,
        StudentAttendanceResult attendanceResult,
        BookingAvailability availability
) {
}
