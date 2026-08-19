package com.classitda.classes.domain;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import java.time.LocalDateTime;

public sealed interface WaitingTrigger {

    record OfferIssued(
            LocalDateTime offeredAt,
            LocalDateTime offerExpiresAt
    ) implements WaitingTrigger {

        public OfferIssued {
            if (offeredAt == null) {
                throw new ClassException(ClassErrorCode.WAITING_OFFERED_AT_REQUIRED);
            }
            if (offerExpiresAt == null) {
                throw new ClassException(
                        ClassErrorCode.WAITING_OFFER_EXPIRES_AT_REQUIRED
                );
            }
        }
    }
}
