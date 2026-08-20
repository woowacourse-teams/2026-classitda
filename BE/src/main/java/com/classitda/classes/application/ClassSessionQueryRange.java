package com.classitda.classes.application;

import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class ClassSessionQueryRange {

    private static final int MAX_CALENDAR_RANGE_DAYS = 42;

    private final LocalDateTime startInclusive;
    private final LocalDateTime endExclusive;

    private ClassSessionQueryRange(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        this.startInclusive = startInclusive;
        this.endExclusive = endExclusive;
    }

    public static ClassSessionQueryRange calendar(LocalDate from, LocalDate to) {
        if (from == null
                || to == null
                || from.isAfter(to)
                || to.equals(LocalDate.MAX)
                || ChronoUnit.DAYS.between(from, to) >= MAX_CALENDAR_RANGE_DAYS) {
            throw new ClassitdaException(CommonErrorCode.INVALID_INPUT);
        }
        return new ClassSessionQueryRange(from.atStartOfDay(), to.plusDays(1).atStartOfDay());
    }

    public LocalDateTime startInclusive() {
        return startInclusive;
    }

    public LocalDateTime endExclusive() {
        return endExclusive;
    }
}
