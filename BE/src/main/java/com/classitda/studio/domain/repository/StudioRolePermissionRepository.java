package com.classitda.studio.domain.repository;

import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.StudioRolePermission;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioRolePermissionRepository extends JpaRepository<StudioRolePermission, Long> {

    boolean existsByStudioRoleIdAndPermissionCode(Long studioRoleId, PermissionCode code);

    boolean existsByStudioRoleIdAndPermissionCodeIn(
            Long studioRoleId,
            Collection<PermissionCode> codes
    );
}
