package com.classitda.classes.presentation.dto;

import com.classitda.classes.application.instructor.enrollment.InstructorEnrollmentCandidateView;
import io.swagger.v3.oas.annotations.media.Schema;

public record InstructorEnrollmentCandidateResponse(
        @Schema(description = "시설 회원 소속 ID", example = "31")
        Long membershipId,

        @Schema(description = "시설에서 사용하는 회원 이름", example = "김민지")
        String name,

        @Schema(description = "회원 프로필 이미지 URL", nullable = true,
                example = "https://images.example.com/minji.png")
        String profileImageUrl
) {

    public static InstructorEnrollmentCandidateResponse from(InstructorEnrollmentCandidateView candidate) {
        return new InstructorEnrollmentCandidateResponse(
                candidate.membershipId(),
                candidate.name(),
                candidate.profileImageUrl()
        );
    }
}
