package com.classitda.studio.domain.repository;

import com.classitda.studio.domain.StudioPolicy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioPolicyRepository extends JpaRepository<StudioPolicy, Long> {

    boolean existsByStudioId(Long studioId);

    Optional<StudioPolicy> findByStudioId(Long studioId);
}
