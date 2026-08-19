package com.classitda.classes.domain;

import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;

public final class WaitingStateMachine {

    public void handle(Waiting waiting, WaitingTrigger trigger) {
        if (trigger == null) {
            throw new ClassException(ClassErrorCode.INVALID_WAITING_TRANSITION);
        }

        switch (trigger) {
            case WaitingTrigger.OfferIssued offerIssued ->
                    waiting.offer(offerIssued.offeredAt(), offerIssued.offerExpiresAt());
            case WaitingTrigger.CancelRequested cancelRequested ->
                    waiting.cancel(cancelRequested.occurredAt());
            case WaitingTrigger.ExpirationReached expirationReached ->
                    waiting.expire(expirationReached.occurredAt());
        }
    }
}
