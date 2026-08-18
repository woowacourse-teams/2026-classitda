package com.classitda.classes.application.instructor.calendar;

import com.classitda.classes.application.instructor.InstructorSessionAccessReader;
import com.classitda.classes.application.instructor.InstructorSessionStatus;
import com.classitda.classes.application.instructor.InstructorSessionStatusResolver;
import com.classitda.classes.domain.repository.projection.ClassSessionCalendarProjection;
import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class InstructorCalendarQueryService {

    private static final int MAX_RANGE_DAYS = 42;

    private final InstructorSessionAccessReader accessReader;
    private final InstructorCalendarScheduleReader scheduleReader;
    private final InstructorSessionStatusResolver statusResolver;
    private final Clock clock;

    public List<InstructorCalendarSummary> findAll(
            Long memberId,
            Long studioId,
            LocalDate from,
            LocalDate to
    ) {
        validateRange(from, to);

        Long requesterMembershipId = accessReader.readRequesterMembershipId(memberId, studioId);
        InstructorCalendarSchedule schedule = scheduleReader.read(
                studioId,
                requesterMembershipId,
                from,
                to
        );
        if (schedule.isEmpty()) {
            return List.of();
        }

        return aggregate(schedule, LocalDateTime.now(clock));
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

    private List<InstructorCalendarSummary> aggregate(
            InstructorCalendarSchedule schedule,
            LocalDateTime now
    ) {
        Map<LocalDate, InstructorCalendarSummary> summaries = new TreeMap<>();

        for (ClassSessionCalendarProjection classSession : schedule.classSessions()) {
            InstructorSessionStatus status = statusResolver.resolve(
                    classSession.getSessionStatus(),
                    classSession.getStartAt(),
                    classSession.getEndAt(),
                    schedule.reservationCloseMinutesBefore(),
                    now
            );
            LocalDate date = classSession.getStartAt().toLocalDate();
            summaries.compute(
                    date,
                    (ignored, summary) -> addStatus(
                            date,
                            summary,
                            status,
                            schedule.requesterMembershipId().equals(
                                    classSession.getInstructorMembershipId()
                            )
                    )
            );
        }

        return summaries.values().stream()
                .filter(summary -> !summary.isEmpty())
                .toList();
    }

    private InstructorCalendarSummary addStatus(
            LocalDate date,
            InstructorCalendarSummary summary,
            InstructorSessionStatus status,
            boolean mine
    ) {
        InstructorCalendarSummary current = summary == null
                ? InstructorCalendarSummary.empty(date)
                : summary;
        return current.add(status, mine);
    }
}
