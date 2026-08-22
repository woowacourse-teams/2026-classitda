package com.classitda.classes.application.student.enrollment;

import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.ClassSessionEnrollment;
import com.classitda.passproduct.domain.MemberPassProduct;
import com.classitda.studio.domain.StudioMembership;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record StudentEnrollmentDetailView(
        Long id,
        StudentEnrollmentDetailStatus status,
        LocalDateTime createdAt,
        LocalDateTime statusChangedAt,
        LocalDateTime attendanceRecordedAt,
        Long waitingPosition,
        LocalDateTime offerExpiresAt,
        ClassSessionDetails classSession,
        UsedPass usedPass,
        Instructor instructor
) {

    static StudentEnrollmentDetailView of(
            ClassSessionEnrollment enrollment,
            StudentEnrollmentDetailStatus status,
            Long waitingPosition
    ) {
        return new StudentEnrollmentDetailView(
                enrollment.getId(),
                status,
                enrollment.getCreatedAt(),
                enrollment.getEnrollmentStatusChangedAt(),
                enrollment.getAttendance().getRecordedAt(),
                waitingPosition,
                status == StudentEnrollmentDetailStatus.OFFERED
                        ? enrollment.getOfferExpiresAt()
                        : null,
                ClassSessionDetails.from(enrollment.getClassSession()),
                UsedPass.from(enrollment.getMemberPassProduct()),
                Instructor.from(enrollment.getClassSession().getInstructorMembership())
        );
    }

    public record ClassSessionDetails(
            Long id,
            String name,
            String description,
            LocalDateTime startAt,
            LocalDateTime endAt,
            LocalDateTime canceledAt
    ) {

        private static ClassSessionDetails from(ClassSession classSession) {
            return new ClassSessionDetails(
                    classSession.getId(),
                    classSession.getName(),
                    classSession.getDescription(),
                    classSession.getStartAt(),
                    classSession.getEndAt(),
                    classSession.getCanceledAt()
            );
        }
    }

    public record UsedPass(
            Long id,
            String name,
            LocalDate startedAt,
            LocalDate expiresAt,
            Integer remainingCount
    ) {

        private static UsedPass from(MemberPassProduct usedPass) {
            if (usedPass == null) {
                return null;
            }
            return new UsedPass(
                    usedPass.getId(),
                    usedPass.getPassProduct().getName(),
                    usedPass.getStartedAt(),
                    usedPass.getExpiresAt(),
                    usedPass.getRemainingCount()
            );
        }
    }

    public record Instructor(
            Long membershipId,
            String name,
            String profileImageUrl,
            String studioName
    ) {

        private static Instructor from(StudioMembership instructor) {
            return new Instructor(
                    instructor.getId(),
                    instructor.getName(),
                    instructor.getMember().getProfileImageUrl(),
                    instructor.getStudio().getName()
            );
        }
    }
}
