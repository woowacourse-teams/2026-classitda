package com.classitda.classes.application.student.calendar;

import com.classitda.classes.application.student.StudentSessionAccessReader;
import com.classitda.classes.application.student.pass.StudentOwnedPasses;
import com.classitda.classes.application.student.pass.StudentOwnedPassesReader;
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
public class StudentCalendarQueryService {

    private static final int MAX_RANGE_DAYS = 42;

    private final StudentSessionAccessReader accessReader;
    private final StudentOwnedPassesReader ownedPassesReader;
    private final StudentCalendarSummaryReader summaryReader;
    private final Clock clock;

    public List<StudentCalendarSummary> findAll(
            Long memberId,
            Long studioId,
            LocalDate from,
            LocalDate to
    ) {
        validateCriteria(from, to);

        Long membershipId = accessReader.readMembershipId(memberId, studioId);
        StudentOwnedPasses ownedPasses = ownedPassesReader.read(membershipId, studioId);
        LocalDateTime now = LocalDateTime.now(clock);

        return summaryReader.read(
                studioId,
                membershipId,
                from,
                to,
                ownedPasses,
                now
        );
    }

    private void validateCriteria(LocalDate from, LocalDate to) {
        if (from == null
                || to == null
                || from.isAfter(to)
                || to.equals(LocalDate.MAX)
                || ChronoUnit.DAYS.between(from, to) >= MAX_RANGE_DAYS) {
            throw new ClassitdaException(CommonErrorCode.INVALID_INPUT);
        }
    }

}
