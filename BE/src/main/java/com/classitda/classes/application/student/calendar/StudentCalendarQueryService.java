package com.classitda.classes.application.student.calendar;

import com.classitda.classes.application.student.StudentSessionAccess;
import com.classitda.classes.application.student.StudentSessionAccessReader;
import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import com.classitda.passproduct.domain.MemberPassProduct;
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
    private final StudentCalendarSummaryReader summaryReader;
    private final Clock clock;

    public List<StudentCalendarSummary> findAll(
            Long memberId,
            Long studioId,
            LocalDate from,
            LocalDate to,
            Long memberPassProductId
    ) {
        validateCriteria(from, to, memberPassProductId);

        StudentSessionAccess access = accessReader.read(memberId, studioId, memberPassProductId);
        MemberPassProduct memberPassProduct = access.memberPassProduct();
        LocalDate effectiveFrom = laterOf(from, memberPassProduct.getStartedAt());
        LocalDate effectiveTo = earlierOf(to, memberPassProduct.getExpiresAt());
        if (effectiveFrom.isAfter(effectiveTo)) {
            return List.of();
        }

        return summaryReader.read(
                studioId,
                access.membershipId(),
                effectiveFrom,
                effectiveTo,
                memberPassProduct.getPassProduct(),
                LocalDateTime.now(clock)
        );
    }

    private void validateCriteria(LocalDate from, LocalDate to, Long memberPassProductId) {
        if (from == null
                || to == null
                || memberPassProductId == null
                || memberPassProductId < 1
                || from.isAfter(to)
                || to.equals(LocalDate.MAX)
                || ChronoUnit.DAYS.between(from, to) >= MAX_RANGE_DAYS) {
            throw new ClassitdaException(CommonErrorCode.INVALID_INPUT);
        }
    }

    private LocalDate laterOf(LocalDate first, LocalDate second) {
        return first.isAfter(second) ? first : second;
    }

    private LocalDate earlierOf(LocalDate first, LocalDate second) {
        return first.isBefore(second) ? first : second;
    }
}
