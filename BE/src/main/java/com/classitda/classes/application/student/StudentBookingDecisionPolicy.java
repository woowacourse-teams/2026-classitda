package com.classitda.classes.application.student;

import com.classitda.classes.domain.BookingWindow;
import org.springframework.stereotype.Component;

@Component
public class StudentBookingDecisionPolicy {

    public StudentBookingDecision decide(StudentBookingContext context) {
        StudentAttendanceResult attendanceResult = resolveAttendanceResult(context);
        StudentBookingRelation bookingRelation = resolveBookingRelation(context, attendanceResult);
        return new StudentBookingDecision(bookingRelation, attendanceResult, resolveAvailability(context));
    }

    private StudentAttendanceResult resolveAttendanceResult(StudentBookingContext context) {
        if (context.reservation().ownAbsentCount() > 0) {
            return StudentAttendanceResult.ABSENT;
        }
        if (context.reservation().ownAttendedCount() > 0) {
            return StudentAttendanceResult.ATTENDED;
        }
        if (context.reservation().ownReservedCount() > 0 && !context.now().isBefore(context.startAt())) {
            return StudentAttendanceResult.ATTENDED;
        }
        return StudentAttendanceResult.NOT_RECORDED;
    }

    private StudentBookingRelation resolveBookingRelation(StudentBookingContext context, StudentAttendanceResult attendanceResult) {
        if (attendanceResult != StudentAttendanceResult.NOT_RECORDED) {
            return StudentBookingRelation.NONE;
        }
        if (context.reservation().ownReservedCount() > 0) {
            return StudentBookingRelation.RESERVED;
        }
        if (context.waiting().ownOfferedCount() > 0) {
            return StudentBookingRelation.OFFERED;
        }
        if (context.waiting().ownWaitingCount() > 0) {
            return StudentBookingRelation.WAITING;
        }
        return StudentBookingRelation.NONE;
    }

    private BookingAvailability resolveAvailability(StudentBookingContext context) {
        // TODO(#46, #68): 사용 가능한 호환 수강권이 없으면 BLOCKED를 반환한다.
        if (context.bookingWindow() == BookingWindow.CLOSED) {
            return BookingAvailability.CLOSED;
        }
        return context.remainingCapacity() > 0
                ? BookingAvailability.RESERVABLE
                : BookingAvailability.WAITLISTABLE;
    }
}
