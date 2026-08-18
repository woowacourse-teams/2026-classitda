package com.classitda.classes.application.instructor.daily;

import com.classitda.classes.application.instructor.InstructorSessionAccessReader;
import com.classitda.classes.application.instructor.InstructorSessionScope;
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
public class InstructorSessionQueryService {

    private final InstructorSessionAccessReader accessReader;
    private final InstructorSessionScheduleReader scheduleReader;
    private final InstructorSessionAssembler assembler;
    private final Clock clock;

    public List<InstructorSessionView> findAll(
            Long memberId,
            Long studioId,
            LocalDate date,
            boolean mineOnly
    ) {
        validateDate(date);

        InstructorSessionScope scope = accessReader.read(memberId, studioId, mineOnly);
        InstructorSessionSchedule schedule = scheduleReader.read(studioId, scope, date);
        if (schedule.isEmpty()) {
            return List.of();
        }

        return assemble(schedule, LocalDateTime.now(clock));
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new ClassitdaException(CommonErrorCode.INVALID_INPUT);
        }
    }

    private List<InstructorSessionView> assemble(
            InstructorSessionSchedule schedule,
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
