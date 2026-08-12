package com.classitda.studio.domain.repository;

import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.StudioRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioRolePermissionRepository extends JpaRepository<StudioRolePermission, Long> {

    boolean existsByStudioRoleIdAndPermissionCode(Long studioRoleId, PermissionCode code);
}
