package com.classitda.classes.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record MemberClassSessionListRequest(
        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "조회할 수업 날짜", example = "2026-08-17")
        LocalDate date
) {
}
