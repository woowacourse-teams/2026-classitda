package com.classitda.classes.application.instructor.daily;

import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.ReservationRepository;
import com.classitda.classes.domain.repository.WaitingRepository;
import com.classitda.classes.domain.repository.projection.ClassSessionDailyProjection;
import com.classitda.classes.domain.repository.projection.ReservationSummaryProjection;
import com.classitda.classes.domain.repository.projection.WaitingSummaryProjection;
import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import com.classitda.studio.domain.StudioPolicy;
import com.classitda.studio.domain.repository.StudioPolicyRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class InstructorDailyScheduleReader {

    private final ClassSessionRepository classSessionRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final StudioPolicyRepository studioPolicyRepository;

    InstructorDailySchedule read(
            Long studioId,
            Long requesterMembershipId,
            LocalDate date
    ) {
        List<ClassSessionDailyProjection> classSessions = classSessionRepository
                .findDailyForInstructor(
                        studioId,
                        date.atStartOfDay(),
                        getRangeEnd(date)
                );

        if (classSessions.isEmpty()) {
            return InstructorDailySchedule.empty();
        }

        return createSchedule(studioId, requesterMembershipId, classSessions);
    }

    private LocalDateTime getRangeEnd(LocalDate date) {
        try {
            return date.plusDays(1).atStartOfDay();
        } catch (DateTimeException exception) {
            throw new ClassitdaException(CommonErrorCode.INVALID_INPUT);
        }
    }

    private InstructorDailySchedule createSchedule(
            Long studioId,
            Long requesterMembershipId,
            List<ClassSessionDailyProjection> classSessions
    ) {
        StudioPolicy studioPolicy = studioPolicyRepository.findByStudioId(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.POLICY_NOT_FOUND));

        List<Long> classSessionIds = classSessions.stream()
                .map(classSession -> classSession.getSession().getId())
                .toList();

        Map<Long, ReservationSummaryProjection> reservationSummaries = reservationRepository
                .findSummaries(classSessionIds, requesterMembershipId).stream()
                .collect(Collectors.toMap(
                        ReservationSummaryProjection::getClassSessionId,
                        Function.identity()
                ));
        Map<Long, WaitingSummaryProjection> waitingSummaries = waitingRepository
                .findSummaries(classSessionIds, requesterMembershipId).stream()
                .collect(Collectors.toMap(
                        WaitingSummaryProjection::getClassSessionId,
                        Function.identity()
                ));

        return new InstructorDailySchedule(
                classSessions,
                reservationSummaries,
                waitingSummaries,
                studioPolicy.getReservationCloseMinutesBefore()
        );
    }
}
