package com.classitda.studio.domain.repository;

import com.classitda.studio.domain.StudioMembership;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioMembershipRepository extends JpaRepository<StudioMembership, Long> {

    Optional<StudioMembership> findByStudioIdAndMemberId(Long studioId, Long memberId);
}
