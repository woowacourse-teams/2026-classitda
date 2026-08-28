package com.classitda.studio.presentation.dto;

import com.classitda.studio.domain.StudioPolicy;

public record StudioPolicyResponse(
        Long id,
        int reservationCloseMinutesBefore,
        int freeCancelMinutesBefore,
        int waitingOfferResponseMinutes,
        int maxHoldDays
) {
    public static StudioPolicyResponse from(StudioPolicy policy) {
        return new StudioPolicyResponse(
                policy.getId(),
                policy.getReservationCloseMinutesBefore(),
                policy.getFreeCancelMinutesBefore(),
                policy.getWaitingOfferResponseMinutes(),
                policy.getMaxHoldDays()
        );
    }
}
