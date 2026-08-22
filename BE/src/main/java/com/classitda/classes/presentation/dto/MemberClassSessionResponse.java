package com.classitda.classes.presentation.dto;

import com.classitda.classes.application.student.BookingAvailability;
import com.classitda.classes.application.student.StudentBookingRelation;
import com.classitda.classes.application.student.daily.StudentDailySessionView;
import com.classitda.classes.domain.AttendanceResult;
import com.classitda.classes.domain.ClassForm;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "회원용 일별 수업 목록 항목")
public record MemberClassSessionResponse(
        Long id,

        @Schema(description = "회원의 활성 수업 신청 ID. 신청 관계가 NONE이면 null입니다.", example = "19")
        Long enrollmentId,

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

        @Schema(description = "RESERVED와 OFFERED 상태가 점유한 현재 좌석 수", minimum = "0", example = "8")
        long reservedCount,

        @Schema(description = "정원에서 현재 점유 좌석 수를 뺀 잔여석", minimum = "0", example = "4")
        long remainingCapacity,

        @Schema(description = "WAITING 상태인 현재 대기 인원", minimum = "0", example = "2")
        long waitingCount,

        @Schema(description = "수업 시작 일시", example = "2026-08-17T20:00:00")
        LocalDateTime startAt,

        @Schema(description = "수업 종료 일시", example = "2026-08-17T21:00:00")
        LocalDateTime endAt,

        @Schema(description = "회원의 현재 예약 관계", example = "NONE")
        StudentBookingRelation bookingRelation,

        @Schema(description = "회원의 출결 결과", example = "NOT_RECORDED")
        AttendanceResult attendanceResult,

        @Schema(description = "회원에게 허용되는 예약 유형", example = "RESERVABLE")
        BookingAvailability availability
) {

    public static MemberClassSessionResponse from(StudentDailySessionView session) {
        return new MemberClassSessionResponse(
                session.id(),
                session.enrollmentId(),
                session.instructorMembershipId(),
                session.instructorName(),
                session.classForm(),
                ClassTypeResponse.of(session.classTypeId(), session.classTypeName()),
                session.className(),
                session.description(),
                session.capacity(),
                session.reservedCount(),
                session.remainingCapacity(),
                session.waitingCount(),
                session.startAt(),
                session.endAt(),
                session.bookingDecision().bookingRelation(),
                session.bookingDecision().attendanceResult(),
                session.bookingDecision().availability()
        );
    }
}
