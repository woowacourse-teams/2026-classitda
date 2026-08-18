package com.classitda.classes.application.instructor.daily;

import com.classitda.classes.application.instructor.InstructorSessionStatus;
import com.classitda.classes.application.instructor.InstructorSessionStatusResolver;
import com.classitda.classes.domain.repository.projection.ClassSessionDailyProjection;
import com.classitda.classes.domain.repository.projection.ReservationSummaryProjection;
import com.classitda.classes.domain.repository.projection.WaitingSummaryProjection;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class InstructorDailySessionAssembler {

    private final InstructorSessionStatusResolver statusResolver;

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

        InstructorSessionStatus status = statusResolver.resolve(
                classSession.getSessionStatus(),
                classSession.getStartAt(),
                classSession.getEndAt(),
                reservationCloseMinutesBefore,
                now
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
