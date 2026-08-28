package com.classitda.studio.presentation.dto;

import com.classitda.studio.domain.StudioRole;

public record StudioRoleResponse(
        String name,
        boolean instructor
) {
    public static StudioRoleResponse from(StudioRole studioRole) {
        return new StudioRoleResponse(studioRole.getName(), studioRole.isInstructor());
    }
}
