package com.classitda.studio.presentation.dto;

import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.StudioMembership;
import java.time.LocalDateTime;

public record StudioMembershipResponse(
        Long id,
        String name,
        String phoneNumber,
        StudioRoleResponse studioRole,
        boolean registered,
        MembershipStatus status,
        LocalDateTime joinedAt
) {
    public static StudioMembershipResponse of(StudioMembership studioMembership, boolean registered) {
        return new StudioMembershipResponse(
                studioMembership.getId(),
                studioMembership.getName(),
                studioMembership.getMember().getPhoneNumber(),
                StudioRoleResponse.from(studioMembership.getStudioRole()),
                registered,
                studioMembership.getStatus(),
                studioMembership.getJoinedAt()
        );
    }
}
