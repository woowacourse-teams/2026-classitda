package com.classitda.classes.presentation.dto;

import com.classitda.classes.application.instructor.calendar.InstructorCalendarSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "강사용 날짜별 수업 집계")
public record InstructorCalendarResponse(
        @Schema(description = "수업 시작 날짜", example = "2026-08-17")
        LocalDate date,

        @Schema(description = "예정 상태 수업 수", minimum = "0", example = "6")
        long scheduledCount,

        @Schema(description = "완료 상태 수업 수", minimum = "0", example = "2")
        long completedCount,

        @Schema(description = "요청자가 담당하는 예정 상태 수업 수", minimum = "0", example = "5")
        long mineScheduledCount,

        @Schema(description = "요청자가 담당하는 완료 상태 수업 수", minimum = "0", example = "1")
        long mineCompletedCount
) {

    public static InstructorCalendarResponse from(InstructorCalendarSummary summary) {
        return new InstructorCalendarResponse(
                summary.date(),
                summary.scheduledCount(),
                summary.completedCount(),
                summary.mineScheduledCount(),
                summary.mineCompletedCount()
        );
    }
}
