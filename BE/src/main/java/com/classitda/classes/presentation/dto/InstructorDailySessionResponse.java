package com.classitda.classes.presentation.dto;

import com.classitda.classes.application.instructor.InstructorSessionStatus;
import com.classitda.classes.application.instructor.daily.InstructorDailySessionView;
import com.classitda.classes.domain.ClassForm;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "강사용 일별 수업 목록 항목")
public record InstructorDailySessionResponse(
        Long id,

        @Schema(description = "담당 강사의 시설 소속 ID", example = "12")
        Long instructorMembershipId,

        String instructorName,

        ClassForm classForm,

        @Schema(description = "시설에 등록된 수업 종류")
        ClassTypeResponse classType,

        String className,

        @Schema(
                description = "수업 안내. 준비물, 수업 장소, 입장 방법 등을 포함할 수 있습니다.",
                example = "수업은 3층 A룸에서 진행합니다. 개인 수건을 준비해 주세요."
        )
        String description,

        @Schema(description = "수업 정원", minimum = "1", example = "12")
        int capacity,

        @Schema(description = "RESERVED와 OFFERED 상태가 점유한 현재 좌석 수", minimum = "0", example = "8")
        long reservedCount,

        @Schema(description = "WAITING 상태인 현재 대기 인원", minimum = "0", example = "2")
        long waitingCount,

        @Schema(description = "수업 시작 일시", example = "2026-08-17T20:00:00")
        LocalDateTime startAt,

        @Schema(description = "수업 종료 일시", example = "2026-08-17T21:00:00")
        LocalDateTime endAt,

        InstructorSessionStatus status,

        @Schema(description = "요청자가 담당하는 수업인지 여부", example = "true")
        boolean mine
) {

    public static InstructorDailySessionResponse from(InstructorDailySessionView view) {
        return new InstructorDailySessionResponse(
                view.id(),
                view.instructorMembershipId(),
                view.instructorName(),
                view.classForm(),
                ClassTypeResponse.of(view.classTypeId(), view.classTypeName()),
                view.className(),
                view.description(),
                view.capacity(),
                view.reservedCount(),
                view.waitingCount(),
                view.startAt(),
                view.endAt(),
                view.status(),
                view.mine()
        );
    }
}
