package com.classitda.classes.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record InstructorCalendarListRequest(
        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "조회 시작 날짜", example = "2026-08-01")
        LocalDate from,

        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "조회 종료 날짜", example = "2026-08-31")
        LocalDate to
) {
}
