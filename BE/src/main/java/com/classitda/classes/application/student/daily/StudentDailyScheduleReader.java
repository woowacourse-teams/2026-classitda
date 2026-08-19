package com.classitda.classes.application.student.daily;

import com.classitda.classes.application.student.pass.StudentOwnedPasses;
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
public class StudentDailyScheduleReader {

    private final ClassSessionRepository classSessionRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final StudioPolicyRepository studioPolicyRepository;

    StudentDailySchedule read(
            Long studioId,
            Long membershipId,
            LocalDate date,
            List<Long> classTypeIds,
            StudentOwnedPasses ownedPasses,
            boolean attendanceHistoryOnly
    ) {
        List<ClassSessionDailyProjection> classSessions = classSessionRepository.findDailyForStudent(
                studioId,
                date.atStartOfDay(),
                getRangeEnd(date),
                classTypeIds,
                membershipId,
                attendanceHistoryOnly
        ).stream()
                .filter(classSession -> ownedPasses.covers(
                        classSession.getClassForm(),
                        classSession.getClassTypeId(),
                        classSession.getStartAt().toLocalDate()
                ))
                .toList();

        if (classSessions.isEmpty()) {
            return StudentDailySchedule.empty();
        }

        return createSchedule(studioId, membershipId, classSessions);
    }

    private LocalDateTime getRangeEnd(LocalDate date) {
        try {
            return date.plusDays(1).atStartOfDay();
        } catch (DateTimeException exception) {
            throw new ClassitdaException(CommonErrorCode.INVALID_INPUT);
        }
    }

    private StudentDailySchedule createSchedule(
            Long studioId,
            Long membershipId,
            List<ClassSessionDailyProjection> classSessions
    ) {
        StudioPolicy studioPolicy = studioPolicyRepository.findByStudioId(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.POLICY_NOT_FOUND));

        List<Long> classSessionIds = classSessions.stream()
                .map(ClassSessionDailyProjection::getClassSessionId)
                .toList();

        Map<Long, ReservationSummaryProjection> reservationSummaries = reservationRepository
                .findSummaries(classSessionIds, membershipId).stream()
                .collect(Collectors.toMap(
                        ReservationSummaryProjection::getClassSessionId,
                        Function.identity()
                ));
        Map<Long, WaitingSummaryProjection> waitingSummaries = waitingRepository
                .findSummaries(classSessionIds, membershipId).stream()
                .collect(Collectors.toMap(
                        WaitingSummaryProjection::getClassSessionId,
                        Function.identity()
                ));

        return new StudentDailySchedule(
                classSessions,
                reservationSummaries,
                waitingSummaries,
                studioPolicy.getReservationCloseMinutesBefore()
        );
    }
}
