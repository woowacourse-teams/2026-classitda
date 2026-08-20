package com.classitda.classes.domain;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ClassSessionDatePlan {

    private final List<LocalDate> dates;

    private ClassSessionDatePlan(List<LocalDate> dates) {
        this.dates = List.copyOf(dates);
    }

    public static ClassSessionDatePlan of(
            Boolean recurring,
            LocalDate classDate,
            List<DayOfWeek> recurringDays,
            LocalDate repeatStartDate,
            LocalDate repeatEndDate
    ) {
        if (recurring == null) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_RECURRENCE);
        }
        if (recurring) {
            return recurring(classDate, recurringDays, repeatStartDate, repeatEndDate);
        }
        return single(classDate, recurringDays, repeatStartDate, repeatEndDate);
    }

    public List<LocalDate> dates() {
        return dates;
    }

    private static ClassSessionDatePlan single(
            LocalDate classDate,
            List<DayOfWeek> recurringDays,
            LocalDate repeatStartDate,
            LocalDate repeatEndDate
    ) {
        if (classDate == null || recurringDays != null || repeatStartDate != null || repeatEndDate != null) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_RECURRENCE);
        }
        return new ClassSessionDatePlan(List.of(classDate));
    }

    private static ClassSessionDatePlan recurring(
            LocalDate classDate,
            List<DayOfWeek> recurringDays,
            LocalDate repeatStartDate,
            LocalDate repeatEndDate
    ) {
        if (classDate != null) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_RECURRENCE);
        }
        validateRecurringDays(recurringDays);
        validateRepeatPeriod(repeatStartDate, repeatEndDate);

        Set<DayOfWeek> selectedDays = new HashSet<>(recurringDays);
        List<LocalDate> dates = new ArrayList<>();
        LocalDate currentDate = repeatStartDate;

        while (true) {
            if (selectedDays.contains(currentDate.getDayOfWeek())) {
                dates.add(currentDate);
            }
            if (currentDate.equals(repeatEndDate)) {
                break;
            }
            currentDate = currentDate.plusDays(1);
        }

        if (dates.isEmpty()) {
            throw new ClassException(ClassErrorCode.CLASS_SESSION_DATES_EMPTY);
        }
        return new ClassSessionDatePlan(dates);
    }

    private static void validateRecurringDays(List<DayOfWeek> recurringDays) {
        if (recurringDays == null
                || recurringDays.isEmpty()
                || recurringDays.stream().anyMatch(dayOfWeek -> dayOfWeek == null)
                || new HashSet<>(recurringDays).size() != recurringDays.size()) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_RECURRING_DAYS);
        }
    }

    private static void validateRepeatPeriod(LocalDate repeatStartDate, LocalDate repeatEndDate) {
        if (repeatStartDate == null || repeatEndDate == null || repeatStartDate.isAfter(repeatEndDate)) {
            throw new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_REPEAT_PERIOD);
        }
    }
}
