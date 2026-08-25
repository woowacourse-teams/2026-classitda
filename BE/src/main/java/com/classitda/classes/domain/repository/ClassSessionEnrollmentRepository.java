package com.classitda.classes.domain.repository;

import com.classitda.classes.domain.enrollment.ClassSessionEnrollment;
import com.classitda.classes.domain.repository.projection.InstructorReservedMemberProjection;
import com.classitda.classes.domain.repository.projection.StudentEnrollmentCalendarEventProjection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClassSessionEnrollmentRepository extends JpaRepository<ClassSessionEnrollment, Long> {

    @EntityGraph(attributePaths = {
            "classSession.instructorMembership.member",
            "classSession.instructorMembership.studio",
            "memberPassProduct.passProduct"
    })
    @Query("""
            SELECT enrollment
            FROM ClassSessionEnrollment enrollment
            WHERE enrollment.id = :enrollmentId
              AND enrollment.classSession.id = :classSessionId
              AND enrollment.classSession.studioId = :studioId
              AND enrollment.membership.id = :membershipId
              AND enrollment.membership.studio.id = :studioId
            """)
    Optional<ClassSessionEnrollment> findDetailForStudent(
            @Param("enrollmentId") Long enrollmentId,
            @Param("classSessionId") Long classSessionId,
            @Param("studioId") Long studioId,
            @Param("membershipId") Long membershipId
    );

    @Query("""
            SELECT COUNT(enrollment.id)
            FROM ClassSessionEnrollment enrollment
            WHERE enrollment.classSession.id = :classSessionId
              AND enrollment.state.status =
                  com.classitda.classes.domain.enrollment.EnrollmentStatus.WAITING
              AND (
                  enrollment.state.statusChangedAt < :statusChangedAt
                  OR (
                      enrollment.state.statusChangedAt = :statusChangedAt
                      AND enrollment.id < :enrollmentId
                  )
              )
            """)
    long countWaitingAhead(
            @Param("classSessionId") Long classSessionId,
            @Param("statusChangedAt") LocalDateTime statusChangedAt,
            @Param("enrollmentId") Long enrollmentId
    );

    @Query("""
            SELECT COUNT(enrollment.id)
            FROM ClassSessionEnrollment enrollment
            WHERE enrollment.classSession.id = :classSessionId
              AND enrollment.state.status IN (
                  com.classitda.classes.domain.enrollment.EnrollmentStatus.RESERVED,
                  com.classitda.classes.domain.enrollment.EnrollmentStatus.OFFERED
              )
            """)
    long countOccupied(@Param("classSessionId") Long classSessionId);

    @Query("""
            SELECT enrollment
            FROM ClassSessionEnrollment enrollment
            WHERE enrollment.id = :enrollmentId
              AND enrollment.classSession.id = :classSessionId
              AND enrollment.classSession.studioId = :studioId
            """)
    Optional<ClassSessionEnrollment> findByIdAndClassSessionIdAndStudioId(
            @Param("enrollmentId") Long enrollmentId,
            @Param("classSessionId") Long classSessionId,
            @Param("studioId") Long studioId
    );

    @Query("""
            SELECT enrollment.id AS enrollmentId,
                   membership.id AS membershipId,
                   membership.name AS memberName,
                   member.profileImageUrl AS profileImageUrl
            FROM ClassSessionEnrollment enrollment
            JOIN enrollment.membership membership
            JOIN membership.member member
            WHERE enrollment.classSession.id = :classSessionId
              AND enrollment.classSession.studioId = :studioId
              AND membership.studio.id = :studioId
              AND enrollment.state.status =
                  com.classitda.classes.domain.enrollment.EnrollmentStatus.RESERVED
            ORDER BY enrollment.state.statusChangedAt ASC, enrollment.id ASC
            """)
    List<InstructorReservedMemberProjection> findReservedMembersForInstructor(
            @Param("studioId") Long studioId,
            @Param("classSessionId") Long classSessionId
    );

    @Query("""
            SELECT enrollment.membership.id
            FROM ClassSessionEnrollment enrollment
            WHERE enrollment.classSession.id = :classSessionId
              AND enrollment.classSession.studioId = :studioId
              AND enrollment.membership.studio.id = :studioId
              AND enrollment.state.status =
                  com.classitda.classes.domain.enrollment.EnrollmentStatus.RESERVED
            """)
    Set<Long> findReservedMembershipIds(
            @Param("studioId") Long studioId,
            @Param("classSessionId") Long classSessionId
    );

    @Query("""
            SELECT enrollment.classSession.classForm AS classForm,
                   classType.id AS classTypeId,
                   enrollment.classSession.startAt AS startAt,
                   enrollment.state.status AS enrollmentStatus
            FROM ClassSessionEnrollment enrollment
            JOIN ClassSessionClassType classSessionClassType
              ON classSessionClassType.classSessionId = enrollment.classSession.id
            JOIN ClassType classType
              ON classType.id = classSessionClassType.classTypeId
            WHERE enrollment.membership.id = :membershipId
              AND enrollment.classSession.studioId = :studioId
              AND classType.studio.id = :studioId
              AND enrollment.classSession.startAt >= :rangeStart
              AND enrollment.classSession.startAt < :rangeEnd
              AND enrollment.classSession.canceledAt IS NULL
              AND enrollment.state.status IN (
                  com.classitda.classes.domain.enrollment.EnrollmentStatus.RESERVED,
                  com.classitda.classes.domain.enrollment.EnrollmentStatus.WAITING
              )
            """)
    List<StudentEnrollmentCalendarEventProjection> findCalendarEventsForStudent(
            @Param("studioId") Long studioId,
            @Param("membershipId") Long membershipId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd
    );
}
