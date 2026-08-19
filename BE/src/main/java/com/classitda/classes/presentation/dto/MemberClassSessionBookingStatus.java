package com.classitda.classes.presentation.dto;

import com.classitda.classes.application.student.StudentBookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "회원에게 표시할 예약 상태. "
                + "AVAILABLE: 예약 가능, WAITING_AVAILABLE: 대기 가능, RESERVED: 예약 완료, "
                + "WAITING: 대기 중, OFFERED: 빈자리 예약 제안, CLOSED: 마감, "
                + "UNAVAILABLE: 수강권으로 예약·대기 불가, ATTENDED: 출석, ABSENT: 결석"
)
public enum MemberClassSessionBookingStatus {
    AVAILABLE,
    WAITING_AVAILABLE,
    RESERVED,
    WAITING,
    OFFERED,
    CLOSED,
    UNAVAILABLE,
    ATTENDED,
    ABSENT;

    public static MemberClassSessionBookingStatus from(StudentBookingStatus status) {
        return valueOf(status.name());
    }
}
