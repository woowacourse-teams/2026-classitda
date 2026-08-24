package com.classitda.classes.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record InstructorEnrollmentCreateRequest(
        @Schema(description = "예약할 회원의 시설 소속 ID", example = "12")
        @NotNull(message = "예약할 회원 소속 ID는 필수입니다.")
        Long membershipId
) {
    public static InstructorEnrollmentCreateRequest from(Long membershipId) {
        return new InstructorEnrollmentCreateRequest(membershipId);
    }
}
