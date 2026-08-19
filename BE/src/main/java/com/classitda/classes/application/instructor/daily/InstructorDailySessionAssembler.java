package com.classitda.classes.application.instructor.daily;

import com.classitda.classes.application.instructor.InstructorSessionStatus;
import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.repository.projection.ClassSessionDailyProjection;
import com.classitda.classes.domain.repository.projection.ReservationSummaryProjection;
import com.classitda.classes.domain.repository.projection.WaitingSummaryProjection;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class InstructorDailySessionAssembler {

    InstructorDailySessionView assemble(
            ClassSessionDailyProjection classSession,
            ReservationSummaryProjection reservationSummary,
            WaitingSummaryProjection waitingSummary,
            int reservationCloseMinutesBefore,
            Long requesterMembershipId,
            LocalDateTime now
    ) {
        long reservedCount = reservationSummary == null ? 0 : reservationSummary.getReservedCount();
        long waitingCount = waitingSummary == null ? 0 : waitingSummary.getWaitingCount();

        ClassSession session = classSession.getSession();
        InstructorSessionStatus status = InstructorSessionStatus.from(
                session.phaseAt(now),
                session.bookingWindowAt(now, reservationCloseMinutesBefore)
        );

        return InstructorDailySessionView.of(
                classSession,
                reservedCount,
                waitingCount,
                status,
                requesterMembershipId.equals(classSession.getInstructorMembershipId())
        );
    }
}
