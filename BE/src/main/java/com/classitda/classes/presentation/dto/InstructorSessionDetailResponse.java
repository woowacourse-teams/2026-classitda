package com.classitda.classes.presentation.dto;

import com.classitda.classes.application.instructor.InstructorSessionStatus;
import com.classitda.classes.application.instructor.enrollment.InstructorSessionDetailView;
import com.classitda.classes.domain.ClassForm;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "강사용 수업 회차 상세와 예약 회원 명단")
public record InstructorSessionDetailResponse(
        Long id,

        @Schema(description = "담당 강사의 시설 소속 ID", example = "12")
        Long instructorMembershipId,

        @Schema(description = "시설에 등록된 담당 강사 이름", example = "이지은 강사")
        String instructorName,

        ClassForm classForm,

        ClassTypeResponse classType,

        @Schema(description = "수업 이름", example = "리포머 밸런스")
        String className,

        @Schema(description = "수업 안내", example = "체어룸에서 진행합니다.")
        String description,

        @Schema(description = "수업 정원", example = "8")
        int capacity,

        @Schema(description = "예약 확정 회원 수", example = "3")
        int reservedCount,

        @Schema(description = "수업 시작 일시", example = "2026-08-17T12:00:00")
        LocalDateTime startAt,

        @Schema(description = "수업 종료 일시", example = "2026-08-17T13:00:00")
        LocalDateTime endAt,

        @Schema(description = "강사용 수업 상태")
        InstructorSessionStatus status,

        @Schema(description = "요청자가 담당하는 수업인지 여부", example = "true")
        boolean mine,

        List<ReservedMemberResponse> reservedMembers
) {

    public static InstructorSessionDetailResponse from(InstructorSessionDetailView detail) {
        return new InstructorSessionDetailResponse(
                detail.id(),
                detail.instructorMembershipId(),
                detail.instructorName(),
                detail.classForm(),
                ClassTypeResponse.of(detail.classTypeId(), detail.classTypeName()),
                detail.className(),
                detail.description(),
                detail.capacity(),
                detail.reservedCount(),
                detail.startAt(),
                detail.endAt(),
                detail.status(),
                detail.mine(),
                detail.reservedMembers().stream()
                        .map(ReservedMemberResponse::from)
                        .toList()
        );
    }

    public record ReservedMemberResponse(
            @Schema(description = "수업 신청 ID", example = "101")
            Long enrollmentId,

            @Schema(description = "예약 회원의 시설 소속 ID", example = "31")
            Long membershipId,

            @Schema(description = "시설에 등록된 회원 이름", example = "김민지")
            String name,

            @Schema(
                    description = "회원 프로필 이미지 URL",
                    example = "https://images.example.com/minji.png",
                    nullable = true
            )
            String profileImageUrl
    ) {

        private static ReservedMemberResponse from(InstructorSessionDetailView.ReservedMember member) {
            return new ReservedMemberResponse(
                    member.enrollmentId(),
                    member.membershipId(),
                    member.name(),
                    member.profileImageUrl()
            );
        }
    }
}
