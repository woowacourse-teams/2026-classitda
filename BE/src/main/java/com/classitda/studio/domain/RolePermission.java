package com.classitda.studio.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "role_permission")
@Entity
public class RolePermission {

    @EmbeddedId
    private RolePermissionId id;

    @MapsId("studioRoleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "studio_role_id", nullable = false)
    private StudioRole studioRole;

    @MapsId("permissionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Builder
    private RolePermission(StudioRole studioRole, Permission permission) {
        this.id = new RolePermissionId();
        this.studioRole = studioRole;
        this.permission = permission;
    }
}
