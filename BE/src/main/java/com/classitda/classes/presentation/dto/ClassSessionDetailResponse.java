package com.classitda.classes.presentation.dto;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.ClassSessionStatus;
import com.classitda.classes.domain.ClassType;
import java.time.LocalDateTime;

public record ClassSessionDetailResponse(
        Long id,
        Long instructorMembershipId,
        String instructorName,
        ClassForm classForm,
        ClassTypeResponse classType,
        String className,
        String description,
        int capacity,
        int durationMinutes,
        LocalDateTime startAt,
        LocalDateTime endAt,
        ClassSessionStatus status
) {

    public static ClassSessionDetailResponse of(
            ClassSession classSession,
            ClassType classType
    ) {
        return new ClassSessionDetailResponse(
                classSession.getId(),
                classSession.getInstructorMembership().getId(),
                classSession.getInstructorMembership().getMember().getName(),
                classSession.getClassForm(),
                ClassTypeResponse.of(classType.getId(), classType.getName()),
                classSession.getName(),
                classSession.getDescription(),
                classSession.getCapacity(),
                classSession.getDurationMinutes(),
                classSession.getStartAt(),
                classSession.getEndAt(),
                classSession.getStatus()
        );
    }
}
