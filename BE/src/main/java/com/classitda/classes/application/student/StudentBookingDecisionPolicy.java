package com.classitda.classes.application.student;

import com.classitda.classes.domain.BookingWindow;
import com.classitda.classes.domain.ReservationStatus;
import com.classitda.classes.domain.WaitingStatus;
import org.springframework.stereotype.Component;

@Component
public class StudentBookingDecisionPolicy {

    public StudentBookingDecision decide(StudentSessionFacts facts) {
        StudentAttendanceResult attendanceResult = resolveAttendanceResult(facts);
        StudentBookingRelation bookingRelation = resolveBookingRelation(facts, attendanceResult);
        return new StudentBookingDecision(bookingRelation, attendanceResult, resolveAvailability(facts));
    }

    private StudentAttendanceResult resolveAttendanceResult(StudentSessionFacts facts) {
        return facts.ownReservationStatus()
                .map(status -> resolveAttendanceResult(status, facts))
                .orElse(StudentAttendanceResult.NOT_RECORDED);
    }

    private StudentAttendanceResult resolveAttendanceResult(ReservationStatus status, StudentSessionFacts facts) {
        return switch (status) {
            case ATTENDED -> StudentAttendanceResult.ATTENDED;
            case ABSENT -> StudentAttendanceResult.ABSENT;
            case RESERVED -> facts.now().isBefore(facts.startAt())
                    ? StudentAttendanceResult.NOT_RECORDED
                    : StudentAttendanceResult.ATTENDED;
            case CANCELED -> StudentAttendanceResult.NOT_RECORDED;
        };
    }

    private StudentBookingRelation resolveBookingRelation(StudentSessionFacts facts, StudentAttendanceResult attendanceResult) {
        if (attendanceResult != StudentAttendanceResult.NOT_RECORDED) {
            return StudentBookingRelation.NONE;
        }
        if (facts.ownReservationStatus().filter(status -> status == ReservationStatus.RESERVED).isPresent()) {
            return StudentBookingRelation.RESERVED;
        }
        return facts.ownWaitingStatus()
                .map(this::resolveWaitingRelation)
                .orElse(StudentBookingRelation.NONE);
    }

    private StudentBookingRelation resolveWaitingRelation(WaitingStatus status) {
        return switch (status) {
            case WAITING -> StudentBookingRelation.WAITING;
            case OFFERED -> StudentBookingRelation.OFFERED;
            case ACCEPTED, EXPIRED, CANCELED -> StudentBookingRelation.NONE;
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
