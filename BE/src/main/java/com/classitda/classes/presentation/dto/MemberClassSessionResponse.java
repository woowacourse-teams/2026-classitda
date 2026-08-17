package com.classitda.classes.presentation.dto;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.repository.projection.ClassSessionDailyProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "회원용 일별 수업 목록 항목")
public record MemberClassSessionResponse(
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

        @Schema(description = "취소되지 않은 현재 예약 인원", minimum = "0", example = "8")
        long reservedCount,

        @Schema(description = "정원에서 현재 예약 인원을 뺀 잔여석", minimum = "0", example = "4")
        long remainingCapacity,

        @Schema(description = "WAITING 상태인 현재 대기 인원", minimum = "0", example = "2")
        long waitingCount,

        @Schema(description = "수업 시작 일시", example = "2026-08-17T20:00:00")
        LocalDateTime startAt,

        @Schema(description = "수업 종료 일시", example = "2026-08-17T21:00:00")
        LocalDateTime endAt,

        MemberClassSessionBookingStatus bookingStatus
) {

    public static MemberClassSessionResponse of(
            ClassSessionDailyProjection classSession,
            long reservedCount,
            long remainingCapacity,
            long waitingCount,
            MemberClassSessionBookingStatus bookingStatus
    ) {
        return new MemberClassSessionResponse(
                classSession.getClassSessionId(),
                classSession.getInstructorMembershipId(),
                classSession.getInstructorName(),
                classSession.getClassForm(),
                ClassTypeResponse.of(classSession.getClassTypeId(), classSession.getClassTypeName()),
                classSession.getClassName(),
                classSession.getDescription(),
                classSession.getCapacity(),
                reservedCount,
                remainingCapacity,
                waitingCount,
                classSession.getStartAt(),
                classSession.getEndAt(),
                bookingStatus
        );
    }
}
