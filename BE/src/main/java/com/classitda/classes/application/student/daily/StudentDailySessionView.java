package com.classitda.classes.application.student.daily;

import com.classitda.classes.application.student.StudentBookingDecision;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.repository.projection.StudentDailySessionProjection;
import java.time.LocalDateTime;

public record StudentDailySessionView(
        Long id,
        Long enrollmentId,
        Long instructorMembershipId,
        String instructorName,
        ClassForm classForm,
        Long classTypeId,
        String classTypeName,
        String className,
        String description,
        int capacity,
        long reservedCount,
        long remainingCapacity,
        long waitingCount,
        LocalDateTime startAt,
        LocalDateTime endAt,
        StudentBookingDecision bookingDecision
) {

    static StudentDailySessionView of(
            StudentDailySessionProjection classSession,
            long reservedCount,
            long remainingCapacity,
            long waitingCount,
            StudentBookingDecision bookingDecision
    ) {
        ClassSession session = classSession.getSession();
        return new StudentDailySessionView(
                session.getId(),
                classSession.getOwnEnrollmentId().orElse(null),
                session.getInstructorMembership().getId(),
                classSession.getInstructorName(),
                session.getClassForm(),
                classSession.getClassTypeId(),
                classSession.getClassTypeName(),
                session.getName(),
                session.getDescription(),
                session.getCapacity(),
                reservedCount,
                remainingCapacity,
                waitingCount,
                session.getStartAt(),
                session.getEndAt(),
                bookingDecision
        );
    }
}
