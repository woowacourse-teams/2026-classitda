package com.classitda.classes.application.instructor.daily;

import com.classitda.classes.application.instructor.InstructorSessionAccessReader;
import com.classitda.classes.application.instructor.InstructorSessionStatus;
import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.repository.projection.ClassSessionDailyProjection;
import com.classitda.classes.domain.repository.projection.ReservationSummaryProjection;
import com.classitda.classes.domain.repository.projection.WaitingSummaryProjection;
import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class InstructorDailyQueryService {

    private final InstructorSessionAccessReader accessReader;
    private final InstructorDailyScheduleReader scheduleReader;
    private final Clock clock;

    public List<InstructorDailySessionView> findAll(
            Long memberId,
            Long studioId,
            LocalDate date
    ) {
        validateDate(date);

        Long requesterMembershipId = accessReader.readRequesterMembershipId(memberId, studioId);
        InstructorDailySchedule schedule = scheduleReader.read(
                studioId,
                requesterMembershipId,
                date
        );
        if (schedule.isEmpty()) {
            return List.of();
        }

        return assemble(
                schedule,
                requesterMembershipId,
                LocalDateTime.now(clock)
        );
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new ClassitdaException(CommonErrorCode.INVALID_INPUT);
        }
    }

    private List<InstructorDailySessionView> assemble(
            InstructorDailySchedule schedule,
            Long requesterMembershipId,
            LocalDateTime now
    ) {
        return schedule.classSessions().stream()
                .map(classSession -> assembleSession(
                        classSession,
                        schedule.reservationSummary(classSession.getSession().getId()),
                        schedule.waitingSummary(classSession.getSession().getId()),
                        schedule.reservationCloseMinutesBefore(),
                        requesterMembershipId,
                        now
                ))
                .toList();
    }

    private InstructorDailySessionView assembleSession(
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
