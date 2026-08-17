package com.classitda.classes.domain.repository;

import com.classitda.classes.domain.Waiting;
import com.classitda.classes.domain.repository.projection.WaitingSummaryProjection;
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
}
