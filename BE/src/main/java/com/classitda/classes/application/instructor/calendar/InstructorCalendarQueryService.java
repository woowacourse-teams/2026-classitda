package com.classitda.classes.application.instructor.calendar;

import com.classitda.classes.application.ClassSessionQueryRange;
import com.classitda.classes.application.instructor.InstructorSessionAccessReader;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.projection.ClassSessionCalendarSummaryProjection;
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
public class InstructorCalendarQueryService {

    private final InstructorSessionAccessReader accessReader;
    private final ClassSessionRepository classSessionRepository;
    private final Clock clock;

    public List<InstructorCalendarSummary> findAll(
            Long memberId,
            Long studioId,
            LocalDate from,
            LocalDate to
    ) {
        ClassSessionQueryRange range = ClassSessionQueryRange.calendar(from, to);

        Long requesterMembershipId = accessReader.readSessionAccess(memberId, studioId)
                .requesterMembershipId();
        return classSessionRepository.findCalendarSummaryForInstructor(
                        studioId,
                        requesterMembershipId,
                        range.startInclusive(),
                        range.endExclusive(),
                        LocalDateTime.now(clock)
                ).stream()
                .map(this::toSummary)
                .toList();
    }

    private InstructorCalendarSummary toSummary(ClassSessionCalendarSummaryProjection projection) {
        return new InstructorCalendarSummary(
                projection.getDate(),
                projection.getScheduled() > 0,
                projection.getCompleted() > 0,
                projection.getMineScheduled() > 0,
                projection.getMineCompleted() > 0
        );
    }
}
