package com.classitda.classes.domain.repository;

import com.classitda.classes.domain.ClassSession;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

    Optional<ClassSession> findByIdAndStudioId(Long classSessionId, Long studioId);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("""
            SELECT classSession
            FROM ClassSession classSession
            WHERE classSession.instructorMembership.id = :instructorMembershipId
              AND classSession.status <> com.classitda.classes.domain.ClassSessionStatus.CANCELED
              AND classSession.startAt < :endAt
              AND classSession.endAt > :startAt
            """)
    List<ClassSession> findActiveOverlaps(
            @Param("instructorMembershipId") Long instructorMembershipId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );
}
