package com.classitda.classes.application.student;

import com.classitda.classes.domain.BookingWindow;
import com.classitda.classes.domain.AttendanceResult;
import com.classitda.classes.domain.EnrollmentStatus;
import org.springframework.stereotype.Component;

@Component
public class StudentBookingDecisionPolicy {

    public StudentBookingDecision decide(StudentSessionFacts facts) {
        AttendanceResult attendanceResult = facts.attendanceResult();
        StudentBookingRelation bookingRelation = resolveBookingRelation(facts);
        return new StudentBookingDecision(bookingRelation, attendanceResult, resolveAvailability(facts));
    }

    private StudentBookingRelation resolveBookingRelation(StudentSessionFacts facts) {
        return facts.ownEnrollmentStatus()
                .map(this::resolveEnrollmentRelation)
                .orElse(StudentBookingRelation.NONE);
    }

    private StudentBookingRelation resolveEnrollmentRelation(EnrollmentStatus status) {
        return switch (status) {
            case RESERVED -> StudentBookingRelation.RESERVED;
            case WAITING -> StudentBookingRelation.WAITING;
            case OFFERED -> StudentBookingRelation.OFFERED;
            case EXPIRED, CANCELED -> StudentBookingRelation.NONE;
        };
    }

    private BookingAvailability resolveAvailability(StudentSessionFacts facts) {
        // TODO(#46, #68): 사용 가능한 호환 수강권이 없으면 BLOCKED를 반환한다.
        if (facts.bookingWindow() == BookingWindow.CLOSED) {
            return BookingAvailability.CLOSED;
        }

        return facts.remainingCapacity() > 0
                ? BookingAvailability.RESERVABLE
                : BookingAvailability.WAITLISTABLE;
    }
}
