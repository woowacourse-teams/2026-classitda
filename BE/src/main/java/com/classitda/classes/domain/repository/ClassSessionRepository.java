package com.classitda.classes.domain.repository;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.repository.projection.ClassSessionDailyProjection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

    Optional<ClassSession> findByIdAndStudioId(Long classSessionId, Long studioId);

    @Query("""
            SELECT CASE WHEN COUNT(classSession) > 0 THEN true ELSE false END
            FROM ClassSession classSession
            WHERE classSession.instructorMembership.id = :instructorMembershipId
              AND classSession.status <> com.classitda.classes.domain.ClassSessionStatus.CANCELED
              AND classSession.startAt < :endAt
              AND classSession.endAt > :startAt
            """)
    boolean existsActiveOverlap(
            @Param("instructorMembershipId") Long instructorMembershipId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    @Query("""
            SELECT classSession.id AS classSessionId,
                   classSession.instructorMembership.id AS instructorMembershipId,
                   classSession.instructorMembership.member.name AS instructorName,
                   classSession.classForm AS classForm,
                   classType.id AS classTypeId,
                   classType.name AS classTypeName,
                   classSession.name AS className,
                   classSession.description AS description,
                   classSession.capacity AS capacity,
                   classSession.startAt AS startAt,
                   classSession.endAt AS endAt,
                   classSession.status AS sessionStatus
            FROM ClassSession classSession,
                 ClassSessionClassType classSessionClassType,
                 ClassType classType
            WHERE classSessionClassType.classSessionId = classSession.id
              AND classType.id = classSessionClassType.classTypeId
              AND classSession.studioId = :studioId
              AND classType.studio.id = :studioId
              AND classSession.startAt >= :rangeStart
              AND classSession.startAt < :rangeEnd
              AND classSession.classForm = :classForm
              AND classType.id IN :classTypeIds
            ORDER BY classSession.startAt ASC, classSession.id ASC
            """)
    List<ClassSessionDailyProjection> findDailyForMemberPass(
            @Param("studioId") Long studioId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("classForm") ClassForm classForm,
            @Param("classTypeIds") List<Long> classTypeIds
    );
}
