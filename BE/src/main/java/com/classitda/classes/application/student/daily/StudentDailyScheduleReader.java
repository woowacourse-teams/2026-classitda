package com.classitda.classes.application.student.daily;

import com.classitda.classes.application.student.pass.StudentOwnedPasses;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.projection.StudentDailySessionProjection;
import com.classitda.studio.domain.StudioPolicy;
import com.classitda.studio.domain.repository.StudioPolicyRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StudentDailyScheduleReader {

    private final ClassSessionRepository classSessionRepository;
    private final StudioPolicyRepository studioPolicyRepository;

    StudentDailySchedule read(
            Long studioId,
            Long membershipId,
            LocalDate date,
            List<Long> classTypeIds,
            StudentOwnedPasses ownedPasses,
            boolean enrollmentHistoryOnly
    ) {
        List<StudentDailySessionProjection> classSessions = classSessionRepository.findDailyForStudent(
                studioId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay(),
                classTypeIds,
                membershipId,
                enrollmentHistoryOnly
        ).stream()
                .filter(classSession -> ownedPasses.covers(
                        classSession.getSession().getClassForm(),
                        classSession.getClassTypeId(),
                        classSession.getSession().getStartAt().toLocalDate()
                ))
                .toList();

        if (classSessions.isEmpty()) {
            return StudentDailySchedule.empty();
        }

        StudioPolicy studioPolicy = studioPolicyRepository.findByStudioId(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.POLICY_NOT_FOUND));

        return new StudentDailySchedule(classSessions, studioPolicy.getReservationCloseMinutesBefore());
    }
}
