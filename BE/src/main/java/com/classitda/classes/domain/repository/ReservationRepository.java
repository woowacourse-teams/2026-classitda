package com.classitda.classes.domain.repository;

import com.classitda.classes.domain.Reservation;
import com.classitda.classes.domain.ReservationStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    int countByClassSessionIdAndStatusNot(Long classSessionId, ReservationStatus status);

    boolean existsByClassSessionIdAndMembershipIdAndStatusNot(
            Long classSessionId,
            Long membershipId,
            ReservationStatus status
    );

    Optional<Reservation> findByIdAndClassSessionId(Long reservationId, Long classSessionId);

    @Query("select count(reservation) > 0 from Reservation reservation "
            + "join reservation.classSession classSession "
            + "where reservation.membership.id = :membershipId "
            + "and reservation.status <> :excludedStatus "
            + "and classSession.startAt < :endAt "
            + "and classSession.endAt > :startAt")
    boolean existsOverlappingByMembershipId(
            @Param("membershipId") Long membershipId,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt,
            @Param("excludedStatus") ReservationStatus excludedStatus
    );
}
