package com.classitda.classes.application.instructor.calendar;

import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.projection.ClassSessionCalendarSummaryProjection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class InstructorCalendarSummaryReader {

    private final ClassSessionRepository classSessionRepository;

    List<InstructorCalendarSummary> read(
            Long studioId,
            Long requesterMembershipId,
            LocalDate from,
            LocalDate to,
            LocalDateTime now
    ) {
        return classSessionRepository.findCalendarSummaryForInstructor(
                        studioId,
                        requesterMembershipId,
                        from.atStartOfDay(),
                        to.plusDays(1).atStartOfDay(),
                        now
                ).stream()
                .map(this::toSummary)
                .toList();
    }

    private InstructorCalendarSummary toSummary(
            ClassSessionCalendarSummaryProjection projection
    ) {
        return new InstructorCalendarSummary(
                projection.getDate(),
                projection.getScheduled() > 0,
                projection.getCompleted() > 0,
                projection.getMineScheduled() > 0,
                projection.getMineCompleted() > 0
        );
    }
}
