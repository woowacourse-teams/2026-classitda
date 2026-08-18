package com.classitda.classes.application.instructor.daily;

import com.classitda.classes.application.instructor.InstructorSessionStatus;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.repository.projection.ClassSessionDailyProjection;
import java.time.LocalDateTime;

public record InstructorSessionView(
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
        long waitingCount,
        LocalDateTime startAt,
        LocalDateTime endAt,
        InstructorSessionStatus status
) {

    static InstructorSessionView of(
            ClassSessionDailyProjection classSession,
            long reservedCount,
            long waitingCount,
            InstructorSessionStatus status
    ) {
        return new InstructorSessionView(
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
                waitingCount,
                classSession.getStartAt(),
                classSession.getEndAt(),
                status
        );
    }
}
