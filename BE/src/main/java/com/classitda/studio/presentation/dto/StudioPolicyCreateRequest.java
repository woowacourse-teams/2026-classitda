package com.classitda.studio.presentation.dto;

import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioPolicy;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record StudioPolicyCreateRequest(
        @NotNull(message = "예약 마감 시간은 필수입니다.")
        @PositiveOrZero(message = "예약 마감 시간은 0분 이상이어야 합니다.")
        @Max(value = 10080, message = "예약 마감 시간은 10080분을 넘을 수 없습니다.")
        Integer reservationCloseMinutesBefore,

        @NotNull(message = "무료 취소 시간은 필수입니다.")
        @PositiveOrZero(message = "무료 취소 시간은 0분 이상이어야 합니다.")
        @Max(value = 10080, message = "무료 취소 시간은 10080분을 넘을 수 없습니다.")
        Integer freeCancelMinutesBefore,

        @NotNull(message = "예약 대기 응답 시간은 필수입니다.")
        @Positive(message = "예약 대기 응답 시간은 1분 이상이어야 합니다.")
        @Max(value = 1440, message = "예약 대기 응답 시간은 1440분을 넘을 수 없습니다.")
        Integer waitingOfferResponseMinutes
) {
    public StudioPolicy toEntity(Studio studio) {
        return StudioPolicy.builder()
                .studio(studio)
                .reservationCloseMinutesBefore(reservationCloseMinutesBefore)
                .freeCancelMinutesBefore(freeCancelMinutesBefore)
                .waitingOfferResponseMinutes(waitingOfferResponseMinutes)
                .build();
    }
}
