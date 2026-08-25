package com.classitda.classes.presentation.dto;

import com.classitda.classes.application.instructor.enrollment.StudioStudentView;
import io.swagger.v3.oas.annotations.media.Schema;

public record StudioStudentResponse(
        @Schema(description = "시설 회원 소속 ID", example = "31")
        Long membershipId,

        @Schema(description = "시설에서 사용하는 회원 이름", example = "김민지")
        String name,

        @Schema(description = "회원 프로필 이미지 URL", nullable = true,
                example = "https://images.example.com/minji.png")
        String profileImageUrl,

        @Schema(description = "현재 수업에 활성 신청으로 추가되어 있는지 여부", example = "true")
        boolean enrolled
) {

    public static StudioStudentResponse from(StudioStudentView candidate) {
        return new StudioStudentResponse(
                candidate.membershipId(),
                candidate.name(),
                candidate.profileImageUrl(),
                candidate.enrolled()
        );
    }
}
