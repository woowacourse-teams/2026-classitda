package com.classitda.member.domain.repository;

import com.classitda.member.domain.Member;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByIdAndWithdrawalRequestedAtIsNull(Long memberId);

    @Query("""
            select member
            from Member member
            where member.cleanedUpAt is null
              and member.cleanupScheduledAt <= :occurredAt
            order by member.cleanupScheduledAt asc, member.id asc
            """)
    List<Member> findCleanupTargets(@Param("occurredAt") LocalDateTime occurredAt, Pageable pageable);

    Optional<Member> findByPhoneNumber(String phoneNumber);
}
