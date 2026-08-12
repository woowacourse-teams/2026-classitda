package com.classitda.studio.domain;

import java.util.EnumSet;
import java.util.Set;
import lombok.Getter;

@Getter
public enum SystemRole {

    OWNER("대표 강사", true, EnumSet.allOf(PermissionCode.class)),
    INSTRUCTOR("일반 강사", true, EnumSet.of(
            PermissionCode.MEMBER_READ,
            PermissionCode.CLASS_TEMPLATE_MANAGE,
            PermissionCode.CLASS_SESSION_MANAGE_OWN,
            PermissionCode.RESERVATION_READ,
            PermissionCode.RESERVATION_MANAGE
    )),
    STUDENT("회원", false, EnumSet.noneOf(PermissionCode.class));

    private final String roleName;
    private final boolean instructor;
    private final Set<PermissionCode> defaultPermissions;

    SystemRole(String roleName, boolean instructor, Set<PermissionCode> defaultPermissions) {
        this.roleName = roleName;
        this.instructor = instructor;
        this.defaultPermissions = defaultPermissions;
    }

    public StudioRole toStudioRole(Studio studio) {
        return StudioRole.builder()
                .studio(studio)
                .name(roleName)
                .systemRole(this)
                .instructor(instructor)
                .build();
    }
}
