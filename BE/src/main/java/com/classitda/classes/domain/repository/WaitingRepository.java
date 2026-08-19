package com.classitda.classes.domain.repository;

import com.classitda.classes.domain.Waiting;
import com.classitda.classes.domain.repository.projection.WaitingSummaryProjection;
import com.classitda.classes.domain.repository.projection.StudentWaitingCalendarEventProjection;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
            SELECT waiting.classSession.id AS classSessionId,
                   SUM(
                       CASE WHEN waiting.status =
                            com.classitda.classes.domain.WaitingStatus.WAITING
                            THEN 1 ELSE 0 END
                   ) AS waitingCount,
                   SUM(
                       CASE WHEN waiting.membership.id = :membershipId
                                  AND waiting.status =
                                      com.classitda.classes.domain.WaitingStatus.WAITING
                            THEN 1 ELSE 0 END
                   ) AS ownWaitingCount,
                   SUM(
                       CASE WHEN waiting.membership.id = :membershipId
                                  AND waiting.status =
                                      com.classitda.classes.domain.WaitingStatus.OFFERED
                            THEN 1 ELSE 0 END
                   ) AS ownOfferedCount
            FROM Waiting waiting
            WHERE waiting.classSession.id IN :classSessionIds
              AND waiting.status IN (
                  com.classitda.classes.domain.WaitingStatus.WAITING,
                  com.classitda.classes.domain.WaitingStatus.OFFERED
              )
            GROUP BY waiting.classSession.id
            """)
    List<WaitingSummaryProjection> findSummaries(
            @Param("classSessionIds") List<Long> classSessionIds,
            @Param("membershipId") Long membershipId
    );

    @Query("""
            SELECT waiting.classSession.id AS classSessionId,
                   waiting.classSession.classForm AS classForm,
                   classType.id AS classTypeId,
                   waiting.classSession.startAt AS startAt
            FROM Waiting waiting,
                 ClassSessionClassType classSessionClassType,
                 ClassType classType
            WHERE classSessionClassType.classSessionId = waiting.classSession.id
              AND classType.id = classSessionClassType.classTypeId
              AND waiting.membership.id = :membershipId
              AND waiting.classSession.studioId = :studioId
              AND classType.studio.id = :studioId
              AND waiting.classSession.startAt >= :rangeStart
              AND waiting.classSession.startAt < :rangeEnd
              AND waiting.classSession.canceledAt IS NULL
              AND waiting.status = com.classitda.classes.domain.WaitingStatus.WAITING
            ORDER BY waiting.classSession.startAt ASC,
                     waiting.classSession.id ASC,
                     classType.id ASC
            """)
    List<StudentWaitingCalendarEventProjection> findCalendarEventsForStudent(
            @Param("studioId") Long studioId,
            @Param("membershipId") Long membershipId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd
    );
}
