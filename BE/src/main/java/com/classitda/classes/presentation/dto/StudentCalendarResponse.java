package com.classitda.classes.presentation.dto;

import com.classitda.classes.application.student.calendar.StudentCalendarSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "학생용 날짜별 수업 상태 요약")
public record StudentCalendarResponse(
        @Schema(description = "수업 시작 날짜", example = "2026-08-17")
        LocalDate date,

        @Schema(description = "시작 시각이 지난 본인 예약 내역 존재 여부", example = "true")
        boolean pastReservation,

        @Schema(description = "시작 전인 예약 확정 수업 존재 여부", example = "true")
        boolean reserved,

        @Schema(description = "시작 전인 대기 중 수업 존재 여부", example = "true")
        boolean waiting
) {

    public static StudentCalendarResponse from(StudentCalendarSummary summary) {
        return new StudentCalendarResponse(
                summary.date(),
                summary.pastReservation(),
                summary.reserved(),
                summary.waiting()
        );
    }
}
