package com.classitda.classes.application.instructor.enrollment;

import com.classitda.classes.application.instructor.InstructorSessionAccess;
import com.classitda.classes.application.instructor.InstructorSessionAccessReader;
import com.classitda.classes.application.instructor.InstructorSessionStatus;
import com.classitda.classes.domain.repository.ClassSessionEnrollmentRepository;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.projection.InstructorReservedMemberProjection;
import com.classitda.classes.domain.repository.projection.InstructorSessionDetailProjection;
import com.classitda.classes.domain.session.ClassSession;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.StudioPolicy;
import com.classitda.studio.domain.repository.StudioMembershipRepository;
import com.classitda.studio.domain.repository.StudioPolicyRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class InstructorSessionQueryService {

    private final ClassSessionEnrollmentRepository enrollmentRepository;
    private final ClassSessionRepository classSessionRepository;
    private final InstructorSessionAccessReader accessReader;
    private final StudioMembershipRepository studioMembershipRepository;
    private final StudioPolicyRepository studioPolicyRepository;
    private final Clock clock;

    public InstructorSessionDetailView findDetail(Long memberId, Long studioId, Long classSessionId) {
        InstructorSessionAccess access = accessReader.readSessionAccess(
                memberId,
                studioId,
                PermissionCode.RESERVATION_READ
        );
        InstructorSessionDetailProjection detail =
                classSessionRepository.findDetailForInstructor(studioId, classSessionId)
                        .orElseThrow(() -> new ClassException(ClassErrorCode.CLASS_SESSION_NOT_FOUND));
        access.validateAccessTo(detail.getInstructorMembershipId());

        ClassSession classSession = detail.getSession();
        StudioPolicy studioPolicy = studioPolicyRepository.findByStudioId(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.POLICY_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now(clock);
        InstructorSessionStatus status = InstructorSessionStatus.from(
                classSession.phaseAt(now),
                classSession.bookingWindowAt(now, studioPolicy.getReservationCloseMinutesBefore())
        );
        List<InstructorReservedMemberProjection> reservedMembers =
                enrollmentRepository.findReservedMembersForInstructor(studioId, classSessionId);

        return InstructorSessionDetailView.of(
                detail,
                status,
                access.requesterMembershipId().equals(detail.getInstructorMembershipId()),
                reservedMembers
        );
    }

    public List<InstructorEnrollmentCandidateView> findAllEnrollmentCandidates(Long memberId, Long studioId, Long classSessionId) {
        InstructorSessionAccess access = accessReader.readSessionAccess(
                memberId,
                studioId,
                PermissionCode.RESERVATION_MANAGE
        );
        ClassSession classSession = classSessionRepository.findByIdAndStudioId(classSessionId, studioId)
                .orElseThrow(() -> new ClassException(ClassErrorCode.CLASS_SESSION_NOT_FOUND));
        access.validateAccessTo(classSession.getInstructorMembership().getId());

        return studioMembershipRepository.findActiveStudents(studioId).stream()
                .map(InstructorEnrollmentCandidateView::from)
                .toList();
    }
}
