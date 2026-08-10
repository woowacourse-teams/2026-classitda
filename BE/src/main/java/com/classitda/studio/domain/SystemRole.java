package com.classitda.studio.domain;

import lombok.Getter;

@Getter
public enum SystemRole {
    OWNER("대표 강사", true),
    INSTRUCTOR("일반 강사", true),
    STUDENT("회원", false);

    private final String roleName;
    private final boolean impliesInstructor;

    SystemRole(String roleName, boolean impliesInstructor) {
        this.roleName = roleName;
        this.impliesInstructor = impliesInstructor;
    }

    public StudioRole toStudioRole(Studio studio) {
        return StudioRole.builder()
                .studio(studio)
                .name(roleName)
                .system(true)
                .impliesInstructor(impliesInstructor)
                .build();
    }
}
