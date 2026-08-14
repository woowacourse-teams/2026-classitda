package com.classitda.studio.presentation.dto;

import com.classitda.studio.domain.StudioRole;

public record StudioRoleResponse(
        Long id,
        String name,
        boolean instructor
) {
    public static StudioRoleResponse from(StudioRole studioRole) {
        return new StudioRoleResponse(studioRole.getId(), studioRole.getName(), studioRole.isInstructor());
    }
}
