package com.classitda.classes.presentation.dto;

import com.classitda.classes.domain.Reservation;
import com.classitda.classes.domain.ReservationStatus;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long classSessionId,
        Long membershipId,
        Long classGuestId,
        String attendeeName,
        ReservationStatus status,
        LocalDateTime reservedAt
) {
    public static ReservationResponse from(Reservation reservation) {
        if (reservation.isGuestReservation()) {
            return new ReservationResponse(
                    reservation.getId(),
                    reservation.getClassSession().getId(),
                    null,
                    reservation.getClassGuest().getId(),
                    reservation.getClassGuest().getName(),
                    reservation.getStatus(),
                    reservation.getReservedAt()
            );
        }
        return new ReservationResponse(
                reservation.getId(),
                reservation.getClassSession().getId(),
                reservation.getMembership().getId(),
                null,
                reservation.getMembership().getName(),
                reservation.getStatus(),
                reservation.getReservedAt()
        );
    }
}
