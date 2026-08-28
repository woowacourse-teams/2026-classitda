package com.classitda.classes.application.student.detail;

import com.classitda.classes.application.student.daily.StudentDailySessionView;
import com.classitda.classes.application.student.enrollment.StudentEnrollmentDetailView;
import com.classitda.classes.domain.repository.projection.StudentSessionDetailProjection;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record StudentSessionDetailView(
        StudentDailySessionView classSession,
        Enrollment enrollment,
        Instructor instructor
) {

    static StudentSessionDetailView of(
            StudentDailySessionView classSession,
            StudentEnrollmentDetailView enrollment,
            StudentSessionDetailProjection projection
    ) {
        return new StudentSessionDetailView(
                classSession,
                Enrollment.from(enrollment),
                new Instructor(
                        classSession.instructorMembershipId(),
                        classSession.instructorName(),
                        projection.getInstructorProfileImageUrl(),
                        projection.getStudioName()
                )
        );
    }

    public record Enrollment(
            Long id,
            LocalDateTime createdAt,
            LocalDateTime statusChangedAt,
            LocalDateTime attendanceRecordedAt,
            Long waitingPosition,
            LocalDateTime offerExpiresAt,
            UsedPass usedPass
    ) {

        private static Enrollment from(StudentEnrollmentDetailView enrollment) {
            if (enrollment == null) {
                return null;
            }

            return new Enrollment(
                    enrollment.id(),
                    enrollment.createdAt(),
                    enrollment.statusChangedAt(),
                    enrollment.attendanceRecordedAt(),
                    enrollment.waitingPosition(),
                    enrollment.offerExpiresAt(),
                    UsedPass.from(enrollment.usedPass())
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

        private static UsedPass from(StudentEnrollmentDetailView.UsedPass usedPass) {
            if (usedPass == null) {
                return null;
            }

            return new UsedPass(
                    usedPass.id(),
                    usedPass.name(),
                    usedPass.startedAt(),
                    usedPass.expiresAt(),
                    usedPass.remainingCount()
            );
        }
    }

    public record Instructor(
            Long membershipId,
            String name,
            String profileImageUrl,
            String studioName
    ) {
    }
}
