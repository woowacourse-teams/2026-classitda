package com.classitda.classes.domain.repository;

import com.classitda.classes.domain.Reservation;
import com.classitda.classes.domain.repository.projection.ReservationSummaryProjection;
import com.classitda.classes.domain.repository.projection.StudentReservationCalendarEventProjection;
import java.time.LocalDateTime;
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
                                     com.classitda.classes.domain.ReservationStatus.NO_SHOW
                            THEN 1 ELSE 0 END
                   ) AS ownNoShowCount
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

    @Query("""
            SELECT reservation.classSession.id AS classSessionId,
                   classType.id AS classTypeId,
                   reservation.classSession.startAt AS startAt,
                   reservation.classSession.endAt AS endAt,
                   reservation.status AS reservationStatus
            FROM Reservation reservation,
                 ClassSessionClassType classSessionClassType,
                 ClassType classType
            WHERE classSessionClassType.classSessionId = reservation.classSession.id
              AND classType.id = classSessionClassType.classTypeId
              AND reservation.membership.id = :membershipId
              AND reservation.classSession.studioId = :studioId
              AND classType.studio.id = :studioId
              AND reservation.classSession.startAt >= :rangeStart
              AND reservation.classSession.startAt < :rangeEnd
              AND reservation.classSession.status <>
                  com.classitda.classes.domain.ClassSessionStatus.CANCELED
              AND reservation.status IN (
                  com.classitda.classes.domain.ReservationStatus.RESERVED,
                  com.classitda.classes.domain.ReservationStatus.ATTENDED
              )
            ORDER BY reservation.classSession.startAt ASC,
                     reservation.classSession.id ASC,
                     classType.id ASC
            """)
    List<StudentReservationCalendarEventProjection> findCalendarEventsForStudent(
            @Param("studioId") Long studioId,
            @Param("membershipId") Long membershipId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd
    );
}
