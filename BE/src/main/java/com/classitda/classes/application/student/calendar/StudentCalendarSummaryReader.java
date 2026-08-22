package com.classitda.classes.application.student.calendar;

import com.classitda.classes.application.ClassSessionQueryRange;
import com.classitda.classes.application.student.pass.StudentOwnedPasses;
import com.classitda.classes.domain.EnrollmentStatus;
import com.classitda.classes.domain.repository.ClassSessionEnrollmentRepository;
import com.classitda.classes.domain.repository.projection.StudentEnrollmentCalendarEventProjection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StudentCalendarSummaryReader {

    private final ClassSessionEnrollmentRepository enrollmentRepository;

    List<StudentCalendarSummary> read(
            Long studioId,
            Long membershipId,
            ClassSessionQueryRange range,
            StudentOwnedPasses ownedPasses,
            LocalDateTime now
    ) {
        Map<LocalDate, StudentCalendarSummary> summaries = new TreeMap<>();
        var events = enrollmentRepository.findCalendarEventsForStudent(
                studioId,
                membershipId,
                range.startInclusive(),
                range.endExclusive()
        );

        for (var event : events) {
            if (!ownedPasses.covers(event.getClassForm(), event.getClassTypeId(), event.getStartAt().toLocalDate())) {
                continue;
            }
            addSummary(summaries, event, now);
        }

        return List.copyOf(summaries.values());
    }

    private void addSummary(
            Map<LocalDate, StudentCalendarSummary> summaries,
            StudentEnrollmentCalendarEventProjection event,
            LocalDateTime now
    ) {
        boolean started = !now.isBefore(event.getStartAt());

        boolean pastReservation = event.getEnrollmentStatus() == EnrollmentStatus.RESERVED && started;
        boolean reserved = event.getEnrollmentStatus() == EnrollmentStatus.RESERVED && !started;
        boolean waiting = event.getEnrollmentStatus() == EnrollmentStatus.WAITING && !started;

        if (!pastReservation && !reserved && !waiting) {
            return;
        }

        StudentCalendarSummary summary = StudentCalendarSummary.of(
                event.getStartAt().toLocalDate(),
                pastReservation,
                reserved,
                waiting
        );
        summaries.merge(summary.date(), summary, StudentCalendarSummary::merge);
    }
}
