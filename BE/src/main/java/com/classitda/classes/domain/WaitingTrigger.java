package com.classitda.classes.domain;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import java.time.LocalDateTime;

public sealed interface WaitingTrigger {

    record OfferIssued(LocalDateTime offeredAt, LocalDateTime offerExpiresAt) implements WaitingTrigger {

        public OfferIssued {
            if (offeredAt == null) {
                throw new ClassException(ClassErrorCode.WAITING_OFFERED_AT_REQUIRED);
            }
            if (offerExpiresAt == null) {
                throw new ClassException(ClassErrorCode.WAITING_OFFER_EXPIRES_AT_REQUIRED);
            }
        }
    }

    record CancelRequested(LocalDateTime occurredAt) implements WaitingTrigger {

        public CancelRequested {
            if (occurredAt == null) {
                throw new ClassException(ClassErrorCode.WAITING_CANCEL_OCCURRED_AT_REQUIRED);
            }
        }
    }

    record ExpirationReached(LocalDateTime occurredAt) implements WaitingTrigger {

        public ExpirationReached {
            if (occurredAt == null) {
                throw new ClassException(ClassErrorCode.WAITING_EXPIRATION_OCCURRED_AT_REQUIRED);
            }
        }
    }

    record OfferAccepted(LocalDateTime occurredAt) implements WaitingTrigger {

        public OfferAccepted {
            if (occurredAt == null) {
                throw new ClassException(ClassErrorCode.WAITING_ACCEPTANCE_OCCURRED_AT_REQUIRED);
            }
        }
    }
}
