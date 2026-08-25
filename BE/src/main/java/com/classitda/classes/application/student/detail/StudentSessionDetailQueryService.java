package com.classitda.classes.application.student.detail;

import com.classitda.classes.application.student.StudentSessionAccessReader;
import com.classitda.classes.application.student.daily.StudentDailySessionAssembler;
import com.classitda.classes.application.student.daily.StudentDailySessionView;
import com.classitda.classes.application.student.enrollment.StudentEnrollmentDetailQueryService;
import com.classitda.classes.application.student.enrollment.StudentEnrollmentDetailView;
import com.classitda.classes.application.student.pass.StudentOwnedPasses;
import com.classitda.classes.application.student.pass.StudentOwnedPassesReader;
import com.classitda.classes.domain.enrollment.EnrollmentStatus;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.projection.StudentSessionDetailProjection;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.studio.domain.StudioPolicy;
import com.classitda.studio.domain.repository.StudioPolicyRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class StudentSessionDetailQueryService {

    private final StudentSessionAccessReader accessReader;
    private final StudentOwnedPassesReader ownedPassesReader;
    private final ClassSessionRepository classSessionRepository;
    private final StudioPolicyRepository studioPolicyRepository;
    private final StudentDailySessionAssembler sessionAssembler;
    private final StudentEnrollmentDetailQueryService enrollmentDetailQueryService;
    private final Clock clock;

    public StudentSessionDetailView findOne(Long memberId, Long studioId, Long classSessionId) {
        Long membershipId = accessReader.readMembershipId(memberId, studioId);
        StudentSessionDetailProjection projection = classSessionRepository
                .findDetailForStudent(studioId, classSessionId, membershipId)
                .orElseThrow(() -> new ClassException(ClassErrorCode.CLASS_SESSION_NOT_FOUND));

        if (projection.getOwnEnrollmentId().isEmpty()) {
            StudentOwnedPasses ownedPasses = ownedPassesReader.read(membershipId, studioId);
            if (!ownedPasses.covers(
                    projection.getSession().getClassForm(),
                    projection.getClassTypeId(),
                    projection.getSession().getStartAt().toLocalDate()
            )) {
                throw new ClassException(ClassErrorCode.CLASS_SESSION_NOT_FOUND);
            }
        }

        LocalDateTime now = LocalDateTime.now(clock);
        validatePastSessionAccess(projection, now);

        StudioPolicy studioPolicy = studioPolicyRepository.findByStudioId(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.POLICY_NOT_FOUND));
        StudentDailySessionView classSession = sessionAssembler.assemble(
                projection,
                studioPolicy.getReservationCloseMinutesBefore(),
                now
        );
        StudentEnrollmentDetailView enrollment = projection.getOwnEnrollmentId()
                .map(enrollmentId -> enrollmentDetailQueryService.findOne(
                        memberId,
                        studioId,
                        classSessionId,
                        enrollmentId
                ))
                .orElse(null);

        return StudentSessionDetailView.of(classSession, enrollment, projection);
    }

    private void validatePastSessionAccess(
            StudentSessionDetailProjection projection,
            LocalDateTime now
    ) {
        boolean pastDate = projection.getSession().getStartAt().toLocalDate()
                .isBefore(now.toLocalDate());
        boolean reserved = projection.getOwnEnrollmentStatus()
                .filter(status -> status == EnrollmentStatus.RESERVED)
                .isPresent();
        if (pastDate && !reserved) {
            throw new ClassException(ClassErrorCode.CLASS_SESSION_NOT_FOUND);
        }
    }
}
