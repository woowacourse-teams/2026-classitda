package com.classitda.classes.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "회원에게 표시할 예약 상태. "
                + "AVAILABLE: 예약 가능, WAITING_AVAILABLE: 대기 가능, RESERVED: 예약 완료, "
                + "WAITING: 대기 중, OFFERED: 빈자리 예약 제안, CLOSED: 마감, "
                + "COMPLETED: 수업 완료, CANCELED: 수업 취소"
)
public enum MemberClassSessionBookingStatus {
    AVAILABLE,
    WAITING_AVAILABLE,
    RESERVED,
    WAITING,
    OFFERED,
    CLOSED,
    COMPLETED,
    CANCELED
}
