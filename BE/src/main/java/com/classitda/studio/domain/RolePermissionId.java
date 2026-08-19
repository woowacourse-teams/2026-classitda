package com.classitda.studio.domain;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Embeddable
public class RolePermissionId implements Serializable {

    private Long studioRoleId;

    private Long permissionId;

    public static RolePermissionId of(Long studioRoleId, Long permissionId) {
        return new RolePermissionId(studioRoleId, permissionId);
    }
}
