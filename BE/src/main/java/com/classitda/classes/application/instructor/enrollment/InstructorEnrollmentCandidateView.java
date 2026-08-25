package com.classitda.classes.application.instructor.enrollment;

import com.classitda.studio.domain.StudioMembership;

public record InstructorEnrollmentCandidateView(
        Long membershipId,
        String name,
        String profileImageUrl
) {

    public static InstructorEnrollmentCandidateView from(StudioMembership membership) {
        return new InstructorEnrollmentCandidateView(
                membership.getId(),
                membership.getName(),
                membership.getMember().getProfileImageUrl()
        );
    }
}
