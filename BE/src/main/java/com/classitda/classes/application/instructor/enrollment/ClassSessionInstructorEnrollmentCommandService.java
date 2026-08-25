package com.classitda.classes.application.instructor.enrollment;

import com.classitda.classes.application.instructor.InstructorSessionAccess;
import com.classitda.classes.application.instructor.InstructorSessionAccessReader;
import com.classitda.classes.domain.enrollment.ClassSessionEnrollment;
import com.classitda.classes.domain.repository.ClassSessionEnrollmentRepository;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.session.ClassSession;
import com.classitda.classes.domain.session.SessionPhase;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.repository.StudioMembershipRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class ClassSessionInstructorEnrollmentCommandService {

    private final ClassSessionEnrollmentRepository classSessionEnrollmentRepository;
    private final ClassSessionRepository classSessionRepository;
    private final InstructorSessionAccessReader accessReader;
    private final StudioMembershipRepository studioMembershipRepository;
    private final Clock clock;

    public void save(Long memberId, Long studioId, Long classSessionId, Long membershipId) {
        InstructorSessionAccess access = accessReader.readSessionAccess(
                memberId,
                studioId,
                PermissionCode.RESERVATION_MANAGE
        );
        ClassSession classSession = getClassSession(studioId, classSessionId);
        access.validateAccessTo(classSession.getInstructorMembership().getId());
        validateScheduled(classSession);

        StudioMembership membership = getActiveMembership(studioId, membershipId);
        validateCapacity(classSession);

        // TODO: 강사가 WAITING/OFFERED 회원을 추가하면 기존 신청을 RESERVED로 전환해 활성 신청 중복 409를 방지한다.
        saveEnrollment(membership, classSession);
    }

    public void cancel(Long memberId, Long studioId, Long classSessionId, Long enrollmentId) {
        InstructorSessionAccess access = accessReader.readSessionAccess(
                memberId,
                studioId,
                PermissionCode.RESERVATION_MANAGE
        );
        ClassSession classSession = getClassSession(studioId, classSessionId);
        access.validateAccessTo(classSession.getInstructorMembership().getId());
        ClassSessionEnrollment enrollment = getEnrollment(studioId, classSessionId, enrollmentId);

        enrollment.cancelReservation(LocalDateTime.now(clock));
    }

    private ClassSession getClassSession(Long studioId, Long classSessionId) {
        return classSessionRepository.findByIdAndStudioId(classSessionId, studioId)
                .orElseThrow(() -> new ClassException(ClassErrorCode.CLASS_SESSION_NOT_FOUND));
    }

    private void validateScheduled(ClassSession classSession) {
        if (classSession.phaseAt(LocalDateTime.now(clock)) != SessionPhase.SCHEDULED) {
            throw new ClassException(ClassErrorCode.ENROLLMENT_SESSION_NOT_SCHEDULED);
        }
    }

    private StudioMembership getActiveMembership(Long studioId, Long membershipId) {
        return studioMembershipRepository.findByIdAndStudioId(membershipId, studioId)
                .filter(membership -> membership.getStatus() == MembershipStatus.ACTIVE)
                .filter(StudioMembership::isStudent)
                .orElseThrow(() -> new ClassException(ClassErrorCode.ENROLLMENT_MEMBER_NOT_FOUND));
    }

    private void validateCapacity(ClassSession classSession) {
        long occupied = classSessionEnrollmentRepository.countOccupied(classSession.getId());
        if (occupied >= classSession.getCapacity()) {
            throw new ClassException(ClassErrorCode.ENROLLMENT_CAPACITY_EXCEEDED);
        }
    }

    private void saveEnrollment(StudioMembership membership, ClassSession classSession) {
        try {
            classSessionEnrollmentRepository.saveAndFlush(
                    ClassSessionEnrollment.reservedWithoutPassProduct(
                            membership,
                            classSession,
                            LocalDateTime.now(clock)
                    )
            );
        } catch (DataIntegrityViolationException exception) {
            throw new ClassException(ClassErrorCode.ENROLLMENT_ALREADY_EXISTS);
        }
    }

    private ClassSessionEnrollment getEnrollment(Long studioId, Long classSessionId, Long enrollmentId) {
        return classSessionEnrollmentRepository
                .findByIdAndClassSessionIdAndStudioId(enrollmentId, classSessionId, studioId)
                .orElseThrow(() -> new ClassException(ClassErrorCode.CLASS_SESSION_ENROLLMENT_NOT_FOUND));
    }
}
