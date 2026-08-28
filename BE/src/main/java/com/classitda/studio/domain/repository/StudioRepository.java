package com.classitda.studio.domain.repository;

import com.classitda.studio.domain.Studio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioRepository extends JpaRepository<Studio, Long> {

    boolean existsByOwnerId(Long memberId);
}
