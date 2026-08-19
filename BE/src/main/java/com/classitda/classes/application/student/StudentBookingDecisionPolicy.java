package com.classitda.classes.application.student;

import com.classitda.classes.domain.BookingWindow;
import org.springframework.stereotype.Component;

@Component
public class StudentBookingDecisionPolicy {

    public StudentBookingDecision decide(StudentBookingContext context) {
        return new StudentBookingDecision(resolveParticipation(context), resolveAvailability(context));
    }

    private StudentParticipation resolveParticipation(StudentBookingContext context) {
        if (context.reservation().ownAbsentCount() > 0) {
            return StudentParticipation.ABSENT;
        }
        if (context.reservation().ownAttendedCount() > 0) {
            return StudentParticipation.ATTENDED;
        }
        if (context.reservation().ownReservedCount() > 0) {
            return context.now().isBefore(context.startAt())
                    ? StudentParticipation.RESERVED
                    : StudentParticipation.ATTENDED;
        }
        if (context.waiting().ownOfferedCount() > 0) {
            return StudentParticipation.OFFERED;
        }
        if (context.waiting().ownWaitingCount() > 0) {
            return StudentParticipation.WAITING;
        }
        return StudentParticipation.NONE;
    }

    private BookingAvailability resolveAvailability(StudentBookingContext context) {
        if (context.bookingWindow() == BookingWindow.CLOSED) {
            return BookingAvailability.CLOSED;
        }
        return context.remainingCapacity() > 0
                ? BookingAvailability.RESERVABLE
                : BookingAvailability.WAITLISTABLE;
    }
}
