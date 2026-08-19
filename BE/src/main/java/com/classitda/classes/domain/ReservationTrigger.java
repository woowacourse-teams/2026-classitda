package com.classitda.classes.domain;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import java.time.LocalDateTime;

public sealed interface ReservationTrigger {

    record CancelRequested(LocalDateTime occurredAt) implements ReservationTrigger {

        public CancelRequested {
            if (occurredAt == null) {
                throw new ClassException(
                        ClassErrorCode.RESERVATION_CANCEL_OCCURRED_AT_REQUIRED
                );
            }
        }
    }
}
