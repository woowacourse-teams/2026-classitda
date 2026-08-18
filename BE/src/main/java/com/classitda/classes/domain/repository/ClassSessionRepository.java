package com.classitda.classes.domain.repository;

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
            SELECT DISTINCT classSession.id AS classSessionId,
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
              AND EXISTS (
                  SELECT memberPassProduct.id
                  FROM MemberPassProduct memberPassProduct
                  JOIN memberPassProduct.passProduct.passProductClassTypes passProductClassType
                  WHERE memberPassProduct.membership.id = :membershipId
                    AND memberPassProduct.passProduct.studio.id = :studioId
                    AND passProductClassType.classType.id = classType.id
                    AND memberPassProduct.startedAt <= :date
                    AND memberPassProduct.expiresAt >= :date
                    AND (
                        :attendedOnly = true
                        OR (
                            memberPassProduct.status = com.classitda.passproduct.domain.MemberPassProductStatus.ACTIVE
                            AND (
                                memberPassProduct.remainingCount IS NULL
                                OR memberPassProduct.remainingCount > 0
                            )
                        )
                    )
              )
              AND (
                  :attendedOnly = false
                  OR (
                      classSession.status <> com.classitda.classes.domain.ClassSessionStatus.CANCELED
                      AND EXISTS (
                          SELECT reservation.id
                          FROM Reservation reservation
                          WHERE reservation.classSession.id = classSession.id
                            AND reservation.membership.id = :membershipId
                            AND reservation.status = com.classitda.classes.domain.ReservationStatus.ATTENDED
                      )
                  )
              )
            ORDER BY classSession.startAt ASC, classSession.id ASC
            """)
    List<ClassSessionDailyProjection> findDailyForStudent(
            @Param("studioId") Long studioId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("membershipId") Long membershipId,
            @Param("date") java.time.LocalDate date,
            @Param("attendedOnly") boolean attendedOnly
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
                           WHEN DATE(class_session.start_at) >= :today
                               AND class_session.end_at > :now
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
                           WHEN DATE(class_session.start_at) >= :today
                               AND class_session.end_at > :now
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
              AND class_session.status <> 'CANCELED'
              AND EXISTS (
                  SELECT 1
                  FROM class_session_class_type
                  JOIN pass_product_class_type
                    ON pass_product_class_type.class_type_id = class_session_class_type.class_type_id
                  JOIN pass_product
                    ON pass_product.id = pass_product_class_type.pass_product_id
                  JOIN member_pass_product
                    ON member_pass_product.pass_product_id = pass_product.id
                  WHERE class_session_class_type.class_session_id = class_session.id
                    AND pass_product.studio_id = :studioId
                    AND member_pass_product.membership_id = :membershipId
                    AND member_pass_product.started_at <= DATE(class_session.start_at)
                    AND member_pass_product.expires_at >= DATE(class_session.start_at)
                    AND (
                        DATE(class_session.start_at) < :today
                        OR (
                            member_pass_product.status = 'ACTIVE'
                            AND (
                                member_pass_product.remaining_count IS NULL
                                OR member_pass_product.remaining_count > 0
                            )
                        )
                    )
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
            @Param("now") LocalDateTime now,
            @Param("today") java.time.LocalDate today
    );
}
