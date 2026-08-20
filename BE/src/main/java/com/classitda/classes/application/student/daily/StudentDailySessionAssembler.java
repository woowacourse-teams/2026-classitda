package com.classitda.classes.application.student.daily;

import com.classitda.classes.application.student.StudentBookingDecision;
import com.classitda.classes.application.student.StudentBookingDecisionPolicy;
import com.classitda.classes.application.student.StudentSessionFacts;
import com.classitda.classes.domain.AttendanceResult;
import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.repository.projection.StudentDailySessionProjection;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StudentDailySessionAssembler {

    private final StudentBookingDecisionPolicy bookingDecisionPolicy;

    StudentDailySessionView assemble(
            StudentDailySessionProjection classSession,
            int reservationCloseMinutesBefore,
            LocalDateTime now
    ) {
        ClassSession session = classSession.getSession();
        long reservedCount = classSession.getReservedCount();
        long waitingCount = classSession.getWaitingCount();
        long remainingCapacity = Math.max((long) session.getCapacity() - reservedCount, 0);

        StudentSessionFacts facts = new StudentSessionFacts(
                session.bookingWindowAt(now, reservationCloseMinutesBefore),
                classSession.getOwnEnrollmentStatus(),
                classSession.getOwnAttendanceResult().orElse(AttendanceResult.NOT_RECORDED),
                remainingCapacity
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
}
