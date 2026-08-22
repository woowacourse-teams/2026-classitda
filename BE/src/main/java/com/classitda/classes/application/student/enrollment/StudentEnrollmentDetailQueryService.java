package com.classitda.classes.application.student.enrollment;

import com.classitda.classes.application.student.StudentSessionAccessReader;
import com.classitda.classes.domain.ClassSessionEnrollment;
import com.classitda.classes.domain.EnrollmentStatus;
import com.classitda.classes.domain.repository.ClassSessionEnrollmentRepository;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class StudentEnrollmentDetailQueryService {

    private final StudentSessionAccessReader accessReader;
    private final ClassSessionEnrollmentRepository enrollmentRepository;

    public StudentEnrollmentDetailView findOne(Long memberId, Long studioId, Long enrollmentId) {
        Long membershipId = accessReader.readMembershipId(memberId, studioId);

        ClassSessionEnrollment enrollment = enrollmentRepository
                .findByIdAndMembershipId(enrollmentId, membershipId)
                .filter(this::isVisible)
                .orElseThrow(() -> new ClassException(ClassErrorCode.CLASS_SESSION_ENROLLMENT_NOT_FOUND));

        StudentEnrollmentDetailStatus status = StudentEnrollmentDetailStatus.resolve(
                enrollment.getEnrollmentStatus(),
                enrollment.getAttendance().getResult(),
                enrollment.getClassSession().isCanceled()
        );
        Long waitingPosition = calculateWaitingPosition(status, enrollment);

        return StudentEnrollmentDetailView.of(enrollment, status, waitingPosition);
    }

    private Long calculateWaitingPosition(
            StudentEnrollmentDetailStatus status,
            ClassSessionEnrollment enrollment
    ) {
        if (status == StudentEnrollmentDetailStatus.WAITING) {
            return enrollmentRepository.countWaitingAhead(
                    enrollment.getClassSession().getId(),
                    enrollment.getEnrollmentStatusChangedAt(),
                    enrollment.getId()
            ) + 1L;
        }

        if (status == StudentEnrollmentDetailStatus.OFFERED) {
            return 0L;
        }

        return null;
    }

    private boolean isVisible(ClassSessionEnrollment enrollment) {
        EnrollmentStatus status = enrollment.getEnrollmentStatus();

        if (status == EnrollmentStatus.EXPIRED) {
            return false;
        }

        return status != EnrollmentStatus.CANCELED || enrollment.getMemberPassProduct() != null;
    }
}
