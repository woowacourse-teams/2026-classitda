package com.classitda.classes.application.instructor.daily;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.projection.InstructorDailySessionProjection;
import com.classitda.studio.domain.StudioPolicy;
import com.classitda.studio.domain.repository.StudioPolicyRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class InstructorScheduleReader {

    private final ClassSessionRepository classSessionRepository;
    private final StudioPolicyRepository studioPolicyRepository;

    InstructorDailySchedule read(Long studioId, LocalDate date) {
        List<InstructorDailySessionProjection> classSessions = classSessionRepository
                .findDailyForInstructor(studioId, date.atStartOfDay(), date.plusDays(1).atStartOfDay());

        if (classSessions.isEmpty()) {
            return InstructorDailySchedule.empty();
        }

        StudioPolicy studioPolicy = studioPolicyRepository.findByStudioId(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.POLICY_NOT_FOUND));

        return new InstructorDailySchedule(classSessions, studioPolicy.getReservationCloseMinutesBefore());
    }

    Slice<InstructorDailySessionProjection> readWithCursor(
            Long studioId,
            InstructorSessionCursor cursor,
            int size,
            ClassForm classForm,
            Long classTypeId
    ) {
        return classSessionRepository.findAllForInstructorWithCursor(
                studioId,
                cursor == null ? null : cursor.startAt(),
                cursor == null ? null : cursor.id(),
                classForm,
                classTypeId,
                PageRequest.ofSize(size)
        );
    }
}
