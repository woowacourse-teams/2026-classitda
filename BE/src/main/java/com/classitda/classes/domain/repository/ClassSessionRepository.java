package com.classitda.classes.domain.repository;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.repository.projection.ClassSessionCalendarSummaryProjection;
import com.classitda.classes.domain.repository.projection.InstructorDailySessionProjection;
import com.classitda.classes.domain.repository.projection.InstructorSessionDetailProjection;
import com.classitda.classes.domain.repository.projection.StudentDailySessionProjection;
import com.classitda.classes.domain.repository.projection.StudentSessionDetailProjection;
import com.classitda.classes.domain.session.ClassSession;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

    boolean existsByInstructorMembershipId(Long membershipId);

    Optional<ClassSession> findByIdAndStudioId(Long classSessionId, Long studioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ClassSession> findForUpdateByIdAndStudioId(Long classSessionId, Long studioId);

    @Query("""
            SELECT classSession AS session,
                   instructorMembership.id AS instructorMembershipId,
                   instructorMembership.name AS instructorName,
                   classType.id AS classTypeId,
                   classType.name AS classTypeName
            FROM ClassSession classSession
            JOIN classSession.instructorMembership instructorMembership
            JOIN ClassSessionClassType classSessionClassType
              ON classSessionClassType.classSessionId = classSession.id
            JOIN ClassType classType
              ON classType.id = classSessionClassType.classTypeId
            WHERE classSession.id = :classSessionId
              AND classSession.studioId = :studioId
              AND classType.studio.id = :studioId
            """)
    Optional<InstructorSessionDetailProjection> findDetailForInstructor(
            @Param("studioId") Long studioId,
            @Param("classSessionId") Long classSessionId
    );

    @Query("""
            SELECT classSession AS session,
                   instructorMembership.name AS instructorName,
                   instructorMember.profileImageUrl AS instructorProfileImageUrl,
                   instructorMembership.studio.name AS studioName,
                   classType.id AS classTypeId,
                   classType.name AS classTypeName,
                   SUM(
                       CASE WHEN enrollment.state.status IN (
                            com.classitda.classes.domain.enrollment.EnrollmentStatus.RESERVED,
                            com.classitda.classes.domain.enrollment.EnrollmentStatus.OFFERED
                       )
                            THEN 1 ELSE 0 END
                   ) AS reservedCount,
                   SUM(
                       CASE WHEN enrollment.state.status =
                            com.classitda.classes.domain.enrollment.EnrollmentStatus.WAITING
                            THEN 1 ELSE 0 END
                   ) AS waitingCount,
                   ownEnrollment.id AS ownEnrollmentId,
                   ownEnrollment.state.status AS ownEnrollmentStatus,
                   ownEnrollment.attendance.result AS ownAttendanceResult
            FROM ClassSession classSession
            JOIN classSession.instructorMembership instructorMembership
            JOIN instructorMembership.member instructorMember
            JOIN ClassSessionClassType classSessionClassType
              ON classSessionClassType.classSessionId = classSession.id
            JOIN ClassType classType
              ON classType.id = classSessionClassType.classTypeId
            LEFT JOIN ClassSessionEnrollment enrollment
              ON enrollment.classSession.id = classSession.id
             AND enrollment.membership.studio.id = :studioId
             AND enrollment.state.status IN (
                 com.classitda.classes.domain.enrollment.EnrollmentStatus.WAITING,
                 com.classitda.classes.domain.enrollment.EnrollmentStatus.OFFERED,
                 com.classitda.classes.domain.enrollment.EnrollmentStatus.RESERVED
             )
            LEFT JOIN ClassSessionEnrollment ownEnrollment
              ON ownEnrollment.classSession.id = classSession.id
             AND ownEnrollment.membership.id = :membershipId
             AND ownEnrollment.membership.studio.id = :studioId
             AND ownEnrollment.state.status IN (
                 com.classitda.classes.domain.enrollment.EnrollmentStatus.WAITING,
                 com.classitda.classes.domain.enrollment.EnrollmentStatus.OFFERED,
                 com.classitda.classes.domain.enrollment.EnrollmentStatus.RESERVED
             )
            WHERE classSession.id = :classSessionId
              AND classSession.studioId = :studioId
              AND classType.studio.id = :studioId
              AND classSession.canceledAt IS NULL
            GROUP BY classSession,
                     instructorMembership.name,
                     instructorMember.profileImageUrl,
                     instructorMembership.studio.name,
                     classType.id,
                     classType.name,
                     ownEnrollment.id,
                     ownEnrollment.state.status,
                     ownEnrollment.attendance.result
            """)
    Optional<StudentSessionDetailProjection> findDetailForStudent(
            @Param("studioId") Long studioId,
            @Param("classSessionId") Long classSessionId,
            @Param("membershipId") Long membershipId
    );

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
            SELECT CASE WHEN COUNT(classSession) > 0 THEN true ELSE false END
            FROM ClassSession classSession
            WHERE classSession.instructorMembership.id = :instructorMembershipId
              AND classSession.id <> :excludedClassSessionId
              AND classSession.canceledAt IS NULL
              AND classSession.startAt < :endAt
              AND classSession.endAt > :startAt
            """)
    boolean existsActiveOverlapExcluding(
            @Param("instructorMembershipId") Long instructorMembershipId,
            @Param("excludedClassSessionId") Long excludedClassSessionId,
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
                            com.classitda.classes.domain.enrollment.EnrollmentStatus.RESERVED,
                            com.classitda.classes.domain.enrollment.EnrollmentStatus.OFFERED
                       )
                            THEN 1 ELSE 0 END
                   ) AS reservedCount,
                   SUM(
                       CASE WHEN enrollment.state.status =
                            com.classitda.classes.domain.enrollment.EnrollmentStatus.WAITING
                            THEN 1 ELSE 0 END
                   ) AS waitingCount,
                   ownEnrollment.id AS ownEnrollmentId,
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
                 com.classitda.classes.domain.enrollment.EnrollmentStatus.WAITING,
                 com.classitda.classes.domain.enrollment.EnrollmentStatus.OFFERED,
                 com.classitda.classes.domain.enrollment.EnrollmentStatus.RESERVED
             )
            LEFT JOIN ClassSessionEnrollment ownEnrollment
              ON ownEnrollment.classSession.id = classSession.id
             AND ownEnrollment.membership.id = :membershipId
             AND ownEnrollment.state.status IN (
                 com.classitda.classes.domain.enrollment.EnrollmentStatus.WAITING,
                 com.classitda.classes.domain.enrollment.EnrollmentStatus.OFFERED,
                 com.classitda.classes.domain.enrollment.EnrollmentStatus.RESERVED
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
                     com.classitda.classes.domain.enrollment.EnrollmentStatus.RESERVED
              )
            GROUP BY classSession,
                     classSession.instructorMembership.name,
                     classType.id,
                     classType.name,
                     ownEnrollment.id,
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
                            com.classitda.classes.domain.enrollment.EnrollmentStatus.RESERVED,
                            com.classitda.classes.domain.enrollment.EnrollmentStatus.OFFERED
                       )
                            THEN 1 ELSE 0 END
                   ) AS reservedCount,
                   SUM(
                       CASE WHEN enrollment.state.status =
                            com.classitda.classes.domain.enrollment.EnrollmentStatus.WAITING
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
                 com.classitda.classes.domain.enrollment.EnrollmentStatus.WAITING,
                 com.classitda.classes.domain.enrollment.EnrollmentStatus.OFFERED,
                 com.classitda.classes.domain.enrollment.EnrollmentStatus.RESERVED
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

    @Query("""
            SELECT classSession AS session,
                   classSession.instructorMembership.name AS instructorName,
                   classType.id AS classTypeId,
                   classType.name AS classTypeName,
                   SUM(
                       CASE WHEN enrollment.state.status IN (
                            com.classitda.classes.domain.enrollment.EnrollmentStatus.RESERVED,
                            com.classitda.classes.domain.enrollment.EnrollmentStatus.OFFERED
                       )
                            THEN 1 ELSE 0 END
                   ) AS reservedCount,
                   SUM(
                       CASE WHEN enrollment.state.status =
                            com.classitda.classes.domain.enrollment.EnrollmentStatus.WAITING
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
                 com.classitda.classes.domain.enrollment.EnrollmentStatus.WAITING,
                 com.classitda.classes.domain.enrollment.EnrollmentStatus.OFFERED,
                 com.classitda.classes.domain.enrollment.EnrollmentStatus.RESERVED
             )
            WHERE classSession.studioId = :studioId
              AND classType.studio.id = :studioId
              AND (
                  :cursorStartAt IS NULL
                  OR classSession.startAt < :cursorStartAt
                  OR (classSession.startAt = :cursorStartAt AND classSession.id < :cursorId)
              )
              AND (:classForm IS NULL OR classSession.classForm = :classForm)
              AND (:classTypeId IS NULL OR classType.id = :classTypeId)
            GROUP BY classSession,
                     classSession.instructorMembership.name,
                     classType.id,
                     classType.name
            ORDER BY classSession.startAt DESC, classSession.id DESC
            """)
    Slice<InstructorDailySessionProjection> findAllForInstructorWithCursor(
            @Param("studioId") Long studioId,
            @Param("cursorStartAt") LocalDateTime cursorStartAt,
            @Param("cursorId") Long cursorId,
            @Param("classForm") ClassForm classForm,
            @Param("classTypeId") Long classTypeId,
            Pageable pageable
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
