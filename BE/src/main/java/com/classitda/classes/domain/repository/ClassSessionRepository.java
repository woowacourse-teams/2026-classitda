package com.classitda.classes.domain.repository;

import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.repository.projection.ClassSessionCalendarSummaryProjection;
import com.classitda.classes.domain.repository.projection.InstructorDailySessionProjection;
import com.classitda.classes.domain.repository.projection.StudentDailySessionProjection;
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
            SELECT classSession AS session,
                   classSession.instructorMembership.name AS instructorName,
                   classType.id AS classTypeId,
                   classType.name AS classTypeName,
                   SUM(
                       CASE WHEN enrollment.state.status IN (
                            com.classitda.classes.domain.EnrollmentStatus.RESERVED,
                            com.classitda.classes.domain.EnrollmentStatus.OFFERED
                       )
                            THEN 1 ELSE 0 END
                   ) AS reservedCount,
                   SUM(
                       CASE WHEN enrollment.state.status =
                            com.classitda.classes.domain.EnrollmentStatus.WAITING
                            THEN 1 ELSE 0 END
                   ) AS waitingCount,
                   ownEnrollment.state.status AS ownEnrollmentStatus,
                   ownEnrollment.attendance.result AS ownAttendanceResult
            FROM ClassSession classSession
            JOIN ClassSessionClassType classSessionClassType
              ON classSessionClassType.classSessionId = classSession.id
            JOIN ClassType classType
              ON classType.id = classSessionClassType.classTypeId
            LEFT JOIN ClassSessionEnrollment enrollment
              ON enrollment.classSession.id = classSession.id
             AND enrollment.state.status IN (
                 com.classitda.classes.domain.EnrollmentStatus.WAITING,
                 com.classitda.classes.domain.EnrollmentStatus.OFFERED,
                 com.classitda.classes.domain.EnrollmentStatus.RESERVED
             )
            LEFT JOIN ClassSessionEnrollment ownEnrollment
              ON ownEnrollment.classSession.id = classSession.id
             AND ownEnrollment.membership.id = :membershipId
             AND ownEnrollment.state.status IN (
                 com.classitda.classes.domain.EnrollmentStatus.WAITING,
                 com.classitda.classes.domain.EnrollmentStatus.OFFERED,
                 com.classitda.classes.domain.EnrollmentStatus.RESERVED
             )
            WHERE classSession.studioId = :studioId
              AND classType.studio.id = :studioId
              AND classSession.startAt >= :rangeStart
              AND classSession.startAt < :rangeEnd
              AND classType.id IN :classTypeIds
              AND classSession.canceledAt IS NULL
              AND (
                  :enrollmentHistoryOnly = false
                  OR ownEnrollment.state.status =
                     com.classitda.classes.domain.EnrollmentStatus.RESERVED
              )
            GROUP BY classSession,
                     classSession.instructorMembership.name,
                     classType.id,
                     classType.name,
                     ownEnrollment.state.status,
                     ownEnrollment.attendance.result
            ORDER BY classSession.startAt ASC, classSession.id ASC
            """)
    List<StudentDailySessionProjection> findDailyForStudent(
            @Param("studioId") Long studioId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("classTypeIds") List<Long> classTypeIds,
            @Param("membershipId") Long membershipId,
            @Param("enrollmentHistoryOnly") boolean enrollmentHistoryOnly
    );

    @Query("""
            SELECT classSession AS session,
                   classSession.instructorMembership.name AS instructorName,
                   classType.id AS classTypeId,
                   classType.name AS classTypeName,
                   SUM(
                       CASE WHEN enrollment.state.status IN (
                            com.classitda.classes.domain.EnrollmentStatus.RESERVED,
                            com.classitda.classes.domain.EnrollmentStatus.OFFERED
                       )
                            THEN 1 ELSE 0 END
                   ) AS reservedCount,
                   SUM(
                       CASE WHEN enrollment.state.status =
                            com.classitda.classes.domain.EnrollmentStatus.WAITING
                            THEN 1 ELSE 0 END
                   ) AS waitingCount
            FROM ClassSession classSession
            JOIN ClassSessionClassType classSessionClassType
              ON classSessionClassType.classSessionId = classSession.id
            JOIN ClassType classType
              ON classType.id = classSessionClassType.classTypeId
            LEFT JOIN ClassSessionEnrollment enrollment
              ON enrollment.classSession.id = classSession.id
             AND enrollment.state.status IN (
                 com.classitda.classes.domain.EnrollmentStatus.WAITING,
                 com.classitda.classes.domain.EnrollmentStatus.OFFERED,
                 com.classitda.classes.domain.EnrollmentStatus.RESERVED
             )
            WHERE classSession.studioId = :studioId
              AND classType.studio.id = :studioId
              AND classSession.startAt >= :rangeStart
              AND classSession.startAt < :rangeEnd
            GROUP BY classSession,
                     classSession.instructorMembership.name,
                     classType.id,
                     classType.name
            ORDER BY classSession.startAt ASC, classSession.id ASC
            """)
    List<InstructorDailySessionProjection> findDailyForInstructor(
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
              AND class_session.canceled_at IS NULL
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
