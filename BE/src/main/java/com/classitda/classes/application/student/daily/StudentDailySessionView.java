package com.classitda.classes.application.student.daily;

import com.classitda.classes.application.student.StudentBookingStatus;
import com.classitda.classes.domain.ClassForm;
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
        return new StudentDailySessionView(
                classSession.getClassSessionId(),
                classSession.getInstructorMembershipId(),
                classSession.getInstructorName(),
                classSession.getClassForm(),
                classSession.getClassTypeId(),
                classSession.getClassTypeName(),
                classSession.getClassName(),
                classSession.getDescription(),
                classSession.getCapacity(),
                reservedCount,
                remainingCapacity,
                waitingCount,
                classSession.getStartAt(),
                classSession.getEndAt(),
                bookingStatus
        );
    }
}
