package com.classitda.classes.presentation.dto;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.session.ClassSession;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.session.SessionPhase;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "회원용 수업 회차 상세 정보")
public record ClassSessionDetailResponse(
        Long id,

        @Schema(description = "담당 강사의 시설 소속 ID", example = "12")
        Long instructorMembershipId,

        String instructorName,

        ClassForm classForm,

        @Schema(description = "시설에 등록된 수업 종류")
        ClassTypeResponse classType,

        String className,

        @Schema(
                description = "회원에게 표시되는 자유 형식의 수업 안내. 준비물, 수업 장소, 입장 방법 등을 포함할 수 있습니다.",
                example = "수업은 3층 A룸에서 진행합니다. 개인 수건을 준비해 주세요."
        )
        String description,

        @Schema(description = "수업 정원", minimum = "1", example = "12")
        int capacity,

        @Schema(
                description = "수업 진행 시간(분)",
                minimum = "1",
                maximum = "1440",
                example = "60"
        )
        int durationMinutes,

        @Schema(description = "수업 시작 일시", example = "2026-08-17T20:00:00")
        LocalDateTime startAt,

        @Schema(
                description = "수업 시작 일시와 진행 시간으로 계산된 종료 일시",
                example = "2026-08-17T21:00:00"
        )
        LocalDateTime endAt,

        @Schema(description = "현재 시각을 기준으로 계산한 수업 진행 단계")
        SessionPhase sessionPhase
) {

    public static ClassSessionDetailResponse of(
            ClassSession classSession,
            ClassType classType,
            SessionPhase sessionPhase
    ) {
        return new ClassSessionDetailResponse(
                classSession.getId(),
                classSession.getInstructorMembership().getId(),
                classSession.getInstructorMembership().getMember().getName(),
                classSession.getClassForm(),
                ClassTypeResponse.of(classType.getId(), classType.getName()),
                classSession.getName(),
                classSession.getDescription(),
                classSession.getCapacity(),
                classSession.getDurationMinutes(),
                classSession.getStartAt(),
                classSession.getEndAt(),
                sessionPhase
        );
    }
}
