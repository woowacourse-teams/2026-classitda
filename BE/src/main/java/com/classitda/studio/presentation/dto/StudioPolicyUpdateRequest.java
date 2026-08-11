package com.classitda.studio.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record StudioPolicyUpdateRequest(
        @PositiveOrZero(message = "예약 마감 시간은 0분 이상이어야 합니다.")
        @Max(value = 10080, message = "예약 마감 시간은 10080분을 넘을 수 없습니다.")
        Integer reservationCloseMinutesBefore,

        @PositiveOrZero(message = "무료 취소 시간은 0분 이상이어야 합니다.")
        @Max(value = 10080, message = "무료 취소 시간은 10080분을 넘을 수 없습니다.")
        Integer freeCancelMinutesBefore,

        @Positive(message = "예약 대기 응답 시간은 1분 이상이어야 합니다.")
        @Max(value = 1440, message = "예약 대기 응답 시간은 1440분을 넘을 수 없습니다.")
        Integer waitingOfferResponseMinutes
) {
}
