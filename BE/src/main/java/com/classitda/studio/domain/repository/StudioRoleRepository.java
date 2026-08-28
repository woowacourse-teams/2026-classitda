package com.classitda.studio.domain.repository;

import com.classitda.studio.domain.StudioRole;
import com.classitda.studio.domain.SystemRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioRoleRepository extends JpaRepository<StudioRole, Long> {

    List<StudioRole> findAllByStudioId(Long studioId);

    Optional<StudioRole> findByStudioIdAndSystemRole(Long studioId, SystemRole systemRole);
}
