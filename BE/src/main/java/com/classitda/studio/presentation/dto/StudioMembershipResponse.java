package com.classitda.studio.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.StudioMembership;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record StudioMembershipResponse(
        Long id,
        String name,
        String phoneNumber,
        StudioRoleResponse studioRole,
        boolean registered,
        MembershipStatus status,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @Schema(type = "string", format = "date-time", example = "2026-08-14T10:00:00")
        LocalDateTime joinedAt
) {
    public static StudioMembershipResponse of(StudioMembership studioMembership, boolean registered) {
        return new StudioMembershipResponse(
                studioMembership.getId(),
                studioMembership.getName(),
                studioMembership.getPhoneNumber(),
                StudioRoleResponse.from(studioMembership.getStudioRole()),
                registered,
                studioMembership.getStatus(),
                studioMembership.getJoinedAt()
        );
    }
}
