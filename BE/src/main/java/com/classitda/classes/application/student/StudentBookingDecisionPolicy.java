package com.classitda.classes.application.student;

import com.classitda.classes.domain.enrollment.AttendanceResult;
import com.classitda.classes.domain.session.BookingWindow;
import com.classitda.classes.domain.enrollment.EnrollmentStatus;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class StudentBookingDecisionPolicy {

    public StudentBookingDecision decide(StudentSessionFacts facts) {
        AttendanceResult attendanceResult = facts.attendanceResult();
        StudentBookingRelation bookingRelation = resolveBookingRelation(facts);

        Optional<BookingAvailability> availability;
        if (bookingRelation == StudentBookingRelation.NONE) {
            availability = Optional.of(resolveAvailability(facts));
        } else {
            availability = Optional.empty();
        }

        return new StudentBookingDecision(bookingRelation, attendanceResult, availability);
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
