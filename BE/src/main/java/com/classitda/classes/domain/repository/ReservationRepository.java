package com.classitda.classes.domain.repository;

import com.classitda.classes.domain.Reservation;
import com.classitda.classes.domain.repository.projection.ReservationSummaryProjection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            SELECT reservation.classSession.id AS classSessionId,
                   COUNT(reservation.id) AS reservedCount,
                   SUM(
                       CASE WHEN reservation.membership.id = :membershipId
                                 AND reservation.status =
                                     com.classitda.classes.domain.ReservationStatus.RESERVED
                            THEN 1 ELSE 0 END
                   ) AS ownReservedCount,
                   SUM(
                       CASE WHEN reservation.membership.id = :membershipId
                                 AND reservation.status =
                                     com.classitda.classes.domain.ReservationStatus.ATTENDED
                            THEN 1 ELSE 0 END
                   ) AS ownAttendedCount,
                   SUM(
                       CASE WHEN reservation.membership.id = :membershipId
                                 AND reservation.status =
                                     com.classitda.classes.domain.ReservationStatus.ABSENT
                            THEN 1 ELSE 0 END
                   ) AS ownAbsentCount
            FROM Reservation reservation
            WHERE reservation.classSession.id IN :classSessionIds
              AND reservation.status <>
                  com.classitda.classes.domain.ReservationStatus.CANCELED
            GROUP BY reservation.classSession.id
            """)
    List<ReservationSummaryProjection> findSummaries(
            @Param("classSessionIds") List<Long> classSessionIds,
            @Param("membershipId") Long membershipId
    );

}
