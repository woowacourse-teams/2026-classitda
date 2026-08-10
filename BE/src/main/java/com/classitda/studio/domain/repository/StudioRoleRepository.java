package com.classitda.studio.domain.repository;

import com.classitda.studio.domain.StudioRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioRoleRepository extends JpaRepository<StudioRole, Long> {

    List<StudioRole> findAllByStudioId(Long studioId);
}
