package com.pheeeew.sigh.domain.repository;

import com.pheeeew.sigh.domain.Sigh;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SighRepository extends JpaRepository<Sigh, Long> {

    Optional<Sigh> findByRequestId(UUID requestId);
}
