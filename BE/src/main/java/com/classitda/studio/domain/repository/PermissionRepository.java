package com.classitda.studio.domain.repository;

import com.classitda.studio.domain.Permission;
import com.classitda.studio.domain.PermissionCode;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    List<Permission> findByCodeIn(Collection<PermissionCode> codes);
}
