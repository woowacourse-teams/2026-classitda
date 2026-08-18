package com.classitda.classes.application.student.daily;

import com.classitda.classes.application.student.StudentSessionAccessReader;
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
public class StudentDailyQueryService {

    private final StudentSessionAccessReader accessReader;
    private final StudentDailyScheduleReader scheduleReader;
    private final StudentDailySessionAssembler assembler;
    private final Clock clock;

    public List<StudentDailySessionView> findAll(
            Long memberId,
            Long studioId,
            LocalDate date
    ) {
        validateCriteria(date);
        LocalDateTime now = LocalDateTime.now(clock);

        Long membershipId = accessReader.readMembershipId(memberId, studioId);

        StudentDailySchedule schedule = scheduleReader.read(
                studioId,
                membershipId,
                date,
                date.isBefore(now.toLocalDate())
        );
        if (schedule.isEmpty()) {
            return List.of();
        }

        return assemble(schedule, now);
    }

    private void validateCriteria(LocalDate date) {
        if (date == null) {
            throw new ClassitdaException(CommonErrorCode.INVALID_INPUT);
        }
    }

    private List<StudentDailySessionView> assemble(
            StudentDailySchedule schedule,
            LocalDateTime now
    ) {
        return schedule.classSessions().stream()
                .map(classSession -> assembler.assemble(
                        classSession,
                        schedule.reservationSummary(classSession.getClassSessionId()),
                        schedule.waitingSummary(classSession.getClassSessionId()),
                        schedule.reservationCloseMinutesBefore(),
                        now
                ))
                .toList();
    }
}
