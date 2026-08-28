package com.classitda.classes.presentation.dto;

import com.classitda.classes.application.student.BookingAvailability;
import com.classitda.classes.application.student.StudentBookingRelation;
import com.classitda.classes.application.student.detail.StudentSessionDetailView;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.enrollment.AttendanceResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "학생용 수업 상세")
public record StudentSessionDetailResponse(
        Long id,
        Enrollment enrollment,
        Instructor instructor,
        ClassForm classForm,
        ClassTypeResponse classType,
        String className,
        String description,
        int capacity,
        long reservedCount,
        long remainingCapacity,
        long waitingCount,
        LocalDateTime startAt,
        LocalDateTime endAt,
        StudentBookingRelation bookingRelation,
        AttendanceResult attendanceResult,
        BookingAvailability availability
) {

    public static StudentSessionDetailResponse from(StudentSessionDetailView detail) {
        var classSession = detail.classSession();
        return new StudentSessionDetailResponse(
                classSession.id(),
                Enrollment.from(detail.enrollment()),
                Instructor.from(detail.instructor()),
                classSession.classForm(),
                ClassTypeResponse.of(classSession.classTypeId(), classSession.classTypeName()),
                classSession.className(),
                classSession.description(),
                classSession.capacity(),
                classSession.reservedCount(),
                classSession.remainingCapacity(),
                classSession.waitingCount(),
                classSession.startAt(),
                classSession.endAt(),
                classSession.bookingDecision().bookingRelation(),
                classSession.bookingDecision().attendanceResult(),
                classSession.bookingDecision().availability().orElse(null)
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

        private static Enrollment from(StudentSessionDetailView.Enrollment enrollment) {
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

        private static UsedPass from(StudentSessionDetailView.UsedPass usedPass) {
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

        private static Instructor from(StudentSessionDetailView.Instructor instructor) {
            return new Instructor(
                    instructor.membershipId(),
                    instructor.name(),
                    instructor.profileImageUrl(),
                    instructor.studioName()
            );
        }
    }
}
