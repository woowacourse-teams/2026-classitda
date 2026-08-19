package com.classitda.classes.domain;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;

public final class ReservationStateMachine {

    public void handle(
            Reservation reservation,
            ReservationTrigger trigger
    ) {
        if (trigger == null) {
            throw new ClassException(ClassErrorCode.INVALID_RESERVATION_TRANSITION);
        }

        switch (trigger) {
            case ReservationTrigger.CancelRequested cancelRequested ->
                    reservation.cancel(cancelRequested.occurredAt());
            case ReservationTrigger.AttendanceConfirmed attendanceConfirmed ->
                    reservation.markAttended(attendanceConfirmed.occurredAt());
            case ReservationTrigger.AbsenceConfirmed absenceConfirmed ->
                    reservation.markAbsent(absenceConfirmed.occurredAt());
        }
    }
}
