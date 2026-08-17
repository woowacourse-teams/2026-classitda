package com.classitda.classes.application.student;

import com.classitda.classes.presentation.dto.MemberClassSessionResponse;
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
public class StudentSessionQueryService {

    private final StudentSessionAccessReader accessReader;
    private final StudentSessionScheduleReader scheduleReader;
    private final StudentSessionAssembler assembler;
    private final Clock clock;

    public List<MemberClassSessionResponse> findAll(
            Long memberId,
            Long studioId,
            LocalDate date,
            Long memberPassProductId
    ) {
        validateCriteria(date, memberPassProductId);

        StudentSessionAccess access = accessReader.read(memberId, studioId, memberPassProductId);
        if (!access.memberPassProduct().isValidOn(date)) {
            return List.of();
        }

        StudentSessionSchedule schedule = scheduleReader.read(
                studioId,
                access.membershipId(),
                date,
                access.memberPassProduct().getPassProduct()
        );
        if (schedule.isEmpty()) {
            return List.of();
        }

        return assemble(schedule, LocalDateTime.now(clock));
    }

    private void validateCriteria(LocalDate date, Long memberPassProductId) {
        if (date == null || memberPassProductId == null || memberPassProductId < 1) {
            throw new ClassitdaException(CommonErrorCode.INVALID_INPUT);
        }
    }

    private List<MemberClassSessionResponse> assemble(
            StudentSessionSchedule schedule,
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
