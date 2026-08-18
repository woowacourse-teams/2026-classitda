package com.classitda.classes.application.instructor.calendar;

import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.projection.ClassSessionCalendarProjection;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class InstructorCalendarScheduleReader {

    private final ClassSessionRepository classSessionRepository;
    private final StudioPolicyRepository studioPolicyRepository;

    InstructorCalendarSchedule read(
            Long studioId,
            Long requesterMembershipId,
            LocalDate from,
            LocalDate to
    ) {
        List<ClassSessionCalendarProjection> classSessions = classSessionRepository
                .findCalendarForInstructor(
                        studioId,
                        from.atStartOfDay(),
                        getRangeEnd(to)
                );

        if (classSessions.isEmpty()) {
            return InstructorCalendarSchedule.empty(requesterMembershipId);
        }

        StudioPolicy studioPolicy = studioPolicyRepository.findByStudioId(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.POLICY_NOT_FOUND));

        return InstructorCalendarSchedule.of(
                classSessions,
                requesterMembershipId,
                studioPolicy.getReservationCloseMinutesBefore()
        );
    }

    private LocalDateTime getRangeEnd(LocalDate to) {
        try {
            return to.plusDays(1).atStartOfDay();
        } catch (DateTimeException exception) {
            throw new ClassitdaException(CommonErrorCode.INVALID_INPUT);
        }
    }
}
