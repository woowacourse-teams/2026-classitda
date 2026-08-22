package com.classitda.classes.application.instructor.daily;

import com.classitda.classes.application.instructor.InstructorSessionAccessReader;
import com.classitda.classes.application.instructor.InstructorSessionStatus;
import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.repository.projection.InstructorDailySessionProjection;
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

    public List<InstructorDailySessionView> findAll(Long memberId, Long studioId, LocalDate date) {
        Long requesterMembershipId = accessReader.readRequesterMembershipId(memberId, studioId);
        InstructorDailySchedule schedule = scheduleReader.read(studioId, date);

        if (schedule.isEmpty()) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now(clock);
        return schedule.classSessions().stream()
                .map(classSession -> assembleSession(
                        classSession,
                        schedule.reservationCloseMinutesBefore(),
                        requesterMembershipId,
                        now
                ))
                .toList();
    }

    private InstructorDailySessionView assembleSession(
            InstructorDailySessionProjection classSession,
            int reservationCloseMinutesBefore,
            Long requesterMembershipId,
            LocalDateTime now
    ) {
        ClassSession session = classSession.getSession();
        InstructorSessionStatus status = InstructorSessionStatus.from(
                session.phaseAt(now),
                session.bookingWindowAt(now, reservationCloseMinutesBefore)
        );

        return InstructorDailySessionView.of(
                classSession,
                status,
                requesterMembershipId.equals(session.getInstructorMembership().getId())
        );
    }
}
