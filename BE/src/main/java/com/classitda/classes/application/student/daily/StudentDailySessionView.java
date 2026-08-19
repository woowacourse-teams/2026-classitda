package com.classitda.classes.application.student.daily;

import com.classitda.classes.application.student.StudentBookingStatus;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.repository.projection.ClassSessionDailyProjection;
import java.time.LocalDateTime;

public record StudentDailySessionView(
        Long id,
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
        StudentBookingStatus bookingStatus
) {

    static StudentDailySessionView of(
            ClassSessionDailyProjection classSession,
            long reservedCount,
            long remainingCapacity,
            long waitingCount,
            StudentBookingStatus bookingStatus
    ) {
        ClassSession session = classSession.getSession();
        return new StudentDailySessionView(
                session.getId(),
                classSession.getInstructorMembershipId(),
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
                bookingStatus
        );
    }
}
