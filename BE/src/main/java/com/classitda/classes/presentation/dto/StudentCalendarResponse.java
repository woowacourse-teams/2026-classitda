package com.classitda.classes.presentation.dto;

import com.classitda.classes.application.student.calendar.StudentCalendarSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "학생용 날짜별 수업 상태 요약")
public record StudentCalendarResponse(
        @Schema(description = "수업 시작 날짜", example = "2026-08-17")
        LocalDate date,

        @Schema(description = "출석 처리된 종료 수업 존재 여부", example = "true")
        boolean attended,

        @Schema(description = "예약 확정된 종료 전 수업 존재 여부", example = "true")
        boolean reserved,

        @Schema(description = "대기 중인 종료 전 수업 존재 여부", example = "true")
        boolean waiting
) {

    public static StudentCalendarResponse from(StudentCalendarSummary summary) {
        return new StudentCalendarResponse(
                summary.date(),
                summary.attended(),
                summary.reserved(),
                summary.waiting()
        );
    }
}
