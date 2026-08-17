package com.classitda.classes.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record MemberClassSessionListRequest(
        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "조회할 수업 날짜", example = "2026-08-17")
        LocalDate date,

        @NotNull
        @Positive
        @Schema(
                description = "예약 가능 수업을 판별할 때 사용할 로그인 회원이 선택한 보유 수강권 ID. "
                        + "수강권의 수업 형태와 수업 종류에 맞는 회차만 조회됩니다.",
                minimum = "1",
                example = "42"
        )
        Long memberPassProductId
) {
}
