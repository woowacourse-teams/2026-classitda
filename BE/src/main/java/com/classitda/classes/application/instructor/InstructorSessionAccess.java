package com.classitda.classes.application.instructor;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;

public record InstructorSessionAccess(
        Long requesterMembershipId,
        boolean allClassSessionsAllowed
) {

    public void validateAccessTo(Long instructorMembershipId) {
        if (allClassSessionsAllowed || requesterMembershipId.equals(instructorMembershipId)) {
            return;
        }

        throw new ClassException(ClassErrorCode.CLASS_SESSION_NOT_FOUND);
    }
}
