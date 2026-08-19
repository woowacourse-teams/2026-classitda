package com.classitda.classes.application.student.daily;

import com.classitda.classes.application.student.StudentBookingDecision;
import com.classitda.classes.application.student.StudentBookingDecisionPolicy;
import com.classitda.classes.application.student.StudentSessionFacts;
import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.ReservationStatus;
import com.classitda.classes.domain.WaitingStatus;
import com.classitda.classes.domain.repository.projection.ClassSessionDailyProjection;
import com.classitda.classes.domain.repository.projection.ReservationSummaryProjection;
import com.classitda.classes.domain.repository.projection.WaitingSummaryProjection;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StudentDailySessionAssembler {

    private final StudentBookingDecisionPolicy bookingDecisionPolicy;

    StudentDailySessionView assemble(
            ClassSessionDailyProjection classSession,
            ReservationSummaryProjection reservationSummary,
            WaitingSummaryProjection waitingSummary,
            int reservationCloseMinutesBefore,
            LocalDateTime now
    ) {
        ClassSession session = classSession.getSession();
        long reservedCount = reservationSummary == null ? 0 : reservationSummary.getReservedCount();
        long waitingCount = waitingSummary == null ? 0 : waitingSummary.getWaitingCount();
        long remainingCapacity = Math.max((long) session.getCapacity() - reservedCount, 0);

        StudentSessionFacts facts = new StudentSessionFacts(
                session.bookingWindowAt(now, reservationCloseMinutesBefore),
                session.getStartAt(),
                resolveOwnReservationStatus(reservationSummary),
                resolveOwnWaitingStatus(waitingSummary),
                remainingCapacity,
                now
        );
        StudentBookingDecision bookingDecision = bookingDecisionPolicy.decide(facts);

        return StudentDailySessionView.of(
                classSession,
                reservedCount,
                remainingCapacity,
                waitingCount,
                bookingDecision
        );
    }

    private Optional<ReservationStatus> resolveOwnReservationStatus(ReservationSummaryProjection summary) {
        if (summary == null) {
            return Optional.empty();
        }
        if (summary.getOwnAbsentCount() > 0) {
            return Optional.of(ReservationStatus.ABSENT);
        }
        if (summary.getOwnAttendedCount() > 0) {
            return Optional.of(ReservationStatus.ATTENDED);
        }
        if (summary.getOwnReservedCount() > 0) {
            return Optional.of(ReservationStatus.RESERVED);
        }
        return Optional.empty();
    }

    private Optional<WaitingStatus> resolveOwnWaitingStatus(WaitingSummaryProjection summary) {
        if (summary == null) {
            return Optional.empty();
        }
        if (summary.getOwnOfferedCount() > 0) {
            return Optional.of(WaitingStatus.OFFERED);
        }
        if (summary.getOwnWaitingCount() > 0) {
            return Optional.of(WaitingStatus.WAITING);
        }
        return Optional.empty();
    }
}
