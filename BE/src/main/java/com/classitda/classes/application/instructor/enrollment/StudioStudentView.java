package com.classitda.classes.application.instructor.enrollment;

import com.classitda.studio.domain.StudioMembership;

public record StudioStudentView(
        Long membershipId,
        String name,
        String profileImageUrl,
        boolean enrolled
) {

    public static StudioStudentView from(StudioMembership membership, boolean enrolled) {
        return new StudioStudentView(
                membership.getId(),
                membership.getName(),
                membership.getProfileImageUrl(),
                enrolled
        );
    }
}
