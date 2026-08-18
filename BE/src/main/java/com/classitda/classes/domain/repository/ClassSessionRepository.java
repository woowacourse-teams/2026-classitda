package com.classitda.classes.domain.repository;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.repository.projection.ClassSessionCalendarSummaryProjection;
import com.classitda.classes.domain.repository.projection.ClassSessionDailyProjection;
import com.classitda.classes.domain.repository.projection.StudentCalendarSummaryProjection;
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
            ORDER BY classSession.startAt ASC, classSession.id ASC
            """)
    List<ClassSessionDailyProjection> findDailyForInstructor(
            @Param("studioId") Long studioId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd
    );

    @Query(value = """
            SELECT DATE(class_session.start_at) AS date,
                   MAX(CASE
                           WHEN class_session.start_at > :now THEN 1
                           ELSE 0
                       END) AS scheduled,
                   MAX(CASE
                           WHEN class_session.end_at <= :now THEN 1
                           ELSE 0
                       END) AS completed,
                   MAX(CASE
                           WHEN class_session.start_at > :now
                               AND class_session.instructor_membership_id = :requesterMembershipId
                               THEN 1
                           ELSE 0
                       END) AS mineScheduled,
                   MAX(CASE
                           WHEN class_session.end_at <= :now
                               AND class_session.instructor_membership_id = :requesterMembershipId
                               THEN 1
                           ELSE 0
                       END) AS mineCompleted
            FROM class_session
            WHERE class_session.studio_id = :studioId
              AND class_session.start_at >= :rangeStart
              AND class_session.start_at < :rangeEnd
              AND class_session.status <> 'CANCELED'
            GROUP BY DATE(class_session.start_at)
            HAVING scheduled = 1 OR completed = 1
            ORDER BY date ASC
            """, nativeQuery = true)
    List<ClassSessionCalendarSummaryProjection> findCalendarSummaryForInstructor(
            @Param("studioId") Long studioId,
            @Param("requesterMembershipId") Long requesterMembershipId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("now") LocalDateTime now
    );

    @Query(value = """
            SELECT DATE(class_session.start_at) AS date,
                   MAX(CASE
                           WHEN class_session.end_at <= :now
                               AND EXISTS (
                                   SELECT 1
                                   FROM reservation
                                   WHERE reservation.class_session_id = class_session.id
                                     AND reservation.membership_id = :membershipId
                                     AND reservation.status = 'ATTENDED'
                               )
                               THEN 1
                           ELSE 0
                       END) AS attended,
                   MAX(CASE
                           WHEN class_session.end_at > :now
                               AND EXISTS (
                                   SELECT 1
                                   FROM reservation
                                   WHERE reservation.class_session_id = class_session.id
                                     AND reservation.membership_id = :membershipId
                                     AND reservation.status = 'RESERVED'
                               )
                               THEN 1
                           ELSE 0
                       END) AS reserved,
                   MAX(CASE
                           WHEN class_session.end_at > :now
                               AND EXISTS (
                                   SELECT 1
                                   FROM waiting
                                   WHERE waiting.class_session_id = class_session.id
                                     AND waiting.membership_id = :membershipId
                                     AND waiting.status = 'WAITING'
                               )
                               THEN 1
                           ELSE 0
                       END) AS waiting
            FROM class_session
            WHERE class_session.studio_id = :studioId
              AND class_session.start_at >= :rangeStart
              AND class_session.start_at < :rangeEnd
              AND class_session.class_form = :classForm
              AND class_session.status <> 'CANCELED'
              AND EXISTS (
                  SELECT 1
                  FROM class_session_class_type
                  JOIN class_type
                    ON class_type.id = class_session_class_type.class_type_id
                  WHERE class_session_class_type.class_session_id = class_session.id
                    AND class_type.studio_id = :studioId
                    AND class_type.id IN (:classTypeIds)
              )
            GROUP BY DATE(class_session.start_at)
            HAVING attended = 1 OR reserved = 1 OR waiting = 1
            ORDER BY date ASC
            """, nativeQuery = true)
    List<StudentCalendarSummaryProjection> findCalendarSummaryForStudent(
            @Param("studioId") Long studioId,
            @Param("membershipId") Long membershipId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("classForm") String classForm,
            @Param("classTypeIds") List<Long> classTypeIds,
            @Param("now") LocalDateTime now
    );
}
