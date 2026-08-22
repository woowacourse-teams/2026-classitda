package com.classitda.classes.application.instructor.daily;

import com.classitda.classes.application.instructor.InstructorSessionStatus;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.repository.projection.InstructorDailySessionProjection;
import java.time.LocalDateTime;

public record InstructorDailySessionView(
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
        InstructorSessionStatus status,
        boolean mine
) {

    static InstructorDailySessionView of(
            InstructorDailySessionProjection classSession,
            InstructorSessionStatus status,
            boolean mine
    ) {
        ClassSession session = classSession.getSession();
        return new InstructorDailySessionView(
                session.getId(),
                session.getInstructorMembership().getId(),
                classSession.getInstructorName(),
                session.getClassForm(),
                classSession.getClassTypeId(),
                classSession.getClassTypeName(),
                session.getName(),
                session.getDescription(),
                session.getCapacity(),
                classSession.getReservedCount(),
                classSession.getWaitingCount(),
                session.getStartAt(),
                session.getEndAt(),
                status,
                mine
        );
    }
}
