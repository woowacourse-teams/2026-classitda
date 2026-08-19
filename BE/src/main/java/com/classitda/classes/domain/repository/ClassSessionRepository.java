package com.classitda.classes.domain.repository;

import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.repository.projection.ClassSessionCalendarSummaryProjection;
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
              AND classSession.canceledAt IS NULL
              AND classSession.startAt < :endAt
              AND classSession.endAt > :startAt
            """)
    boolean existsActiveOverlap(
            @Param("instructorMembershipId") Long instructorMembershipId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    @Query("""
            SELECT DISTINCT classSession AS session,
                   classSession.instructorMembership.id AS instructorMembershipId,
                   classSession.instructorMembership.member.name AS instructorName,
                   classType.id AS classTypeId,
                   classType.name AS classTypeName
            FROM ClassSession classSession,
                 ClassSessionClassType classSessionClassType,
                 ClassType classType
            WHERE classSessionClassType.classSessionId = classSession.id
              AND classType.id = classSessionClassType.classTypeId
              AND classSession.studioId = :studioId
              AND classType.studio.id = :studioId
              AND classSession.startAt >= :rangeStart
              AND classSession.startAt < :rangeEnd
              AND classType.id IN :classTypeIds
              AND classSession.canceledAt IS NULL
              AND (
                  :attendanceHistoryOnly = false
                  OR EXISTS (
                      SELECT reservation.id
                      FROM Reservation reservation
                      WHERE reservation.classSession.id = classSession.id
                        AND reservation.membership.id = :membershipId
                        AND reservation.status IN (
                            com.classitda.classes.domain.ReservationStatus.RESERVED,
                            com.classitda.classes.domain.ReservationStatus.ATTENDED,
                            com.classitda.classes.domain.ReservationStatus.ABSENT
                        )
                  )
              )
            ORDER BY classSession.startAt ASC, classSession.id ASC
            """)
    List<ClassSessionDailyProjection> findDailyForStudent(
            @Param("studioId") Long studioId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("classTypeIds") List<Long> classTypeIds,
            @Param("membershipId") Long membershipId,
            @Param("attendanceHistoryOnly") boolean attendanceHistoryOnly
    );

    @Query("""
            SELECT classSession AS session,
                   classSession.instructorMembership.id AS instructorMembershipId,
                   classSession.instructorMembership.member.name AS instructorName,
                   classType.id AS classTypeId,
                   classType.name AS classTypeName
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

}
