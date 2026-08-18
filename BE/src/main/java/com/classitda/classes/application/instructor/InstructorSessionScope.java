package com.classitda.classes.application.instructor;

public record InstructorSessionScope(
        Long requesterMembershipId,
        Long instructorMembershipId
) {
}
