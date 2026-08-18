package com.classitda.classes.application.student.calendar;

import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.projection.StudentCalendarSummaryProjection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StudentCalendarSummaryReader {

    private final ClassSessionRepository classSessionRepository;

    List<StudentCalendarSummary> read(
            Long studioId,
            Long membershipId,
            LocalDate from,
            LocalDate to,
            LocalDateTime now
    ) {
        return classSessionRepository.findCalendarSummaryForStudent(
                        studioId,
                        membershipId,
                        from.atStartOfDay(),
                        to.plusDays(1).atStartOfDay(),
                        now,
                        now.toLocalDate()
                ).stream()
                .map(this::toSummary)
                .toList();
    }

    private StudentCalendarSummary toSummary(StudentCalendarSummaryProjection projection) {
        return new StudentCalendarSummary(
                projection.getDate(),
                projection.getAttended() > 0,
                projection.getReserved() > 0,
                projection.getWaiting() > 0
        );
    }
}
