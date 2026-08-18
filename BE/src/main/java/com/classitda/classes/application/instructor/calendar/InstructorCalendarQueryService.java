package com.classitda.classes.application.instructor.calendar;

import com.classitda.classes.application.instructor.InstructorSessionAccessReader;
import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class InstructorCalendarQueryService {

    private static final int MAX_RANGE_DAYS = 42;

    private final InstructorSessionAccessReader accessReader;
    private final InstructorCalendarSummaryReader summaryReader;
    private final Clock clock;

    public List<InstructorCalendarSummary> findAll(
            Long memberId,
            Long studioId,
            LocalDate from,
            LocalDate to
    ) {
        validateRange(from, to);

        Long requesterMembershipId = accessReader.readRequesterMembershipId(memberId, studioId);
        return summaryReader.read(
                studioId,
                requesterMembershipId,
                from,
                to,
                LocalDateTime.now(clock)
        );
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null
                || to == null
                || from.isAfter(to)
                || to.equals(LocalDate.MAX)
                || ChronoUnit.DAYS.between(from, to) >= MAX_RANGE_DAYS) {
            throw new ClassitdaException(CommonErrorCode.INVALID_INPUT);
        }
    }
}
