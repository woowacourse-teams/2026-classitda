package com.classitda.classes.presentation.dto;

import com.classitda.classes.application.student.enrollment.StudentEnrollmentDetailStatus;
import com.classitda.classes.application.student.enrollment.StudentEnrollmentDetailView;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "학생 본인의 수업 신청 상세")
public record StudentEnrollmentDetailResponse(
        @Schema(description = "수업 신청 ID", example = "19")
        Long id,

        @Schema(description = "학생 신청 상세 화면 상태", example = "OFFERED")
        StudentEnrollmentDetailStatus status,

        @Schema(description = "신청 생명주기 생성 시각", example = "2026-08-06T15:47:00")
        LocalDateTime createdAt,

        @Schema(description = "현재 신청 상태로 변경된 시각", example = "2026-08-06T15:47:00")
        LocalDateTime statusChangedAt,

        @Schema(description = "출석 또는 결석 기록 시각", example = "2026-08-12T11:50:00")
        LocalDateTime attendanceRecordedAt,

        @Schema(description = "WAITING의 현재 순번, OFFERED는 0, 나머지는 null", example = "0")
        Long waitingPosition,

        @Schema(description = "OFFERED 상태의 제안 만료 시각", example = "2026-08-06T16:47:00")
        LocalDateTime offerExpiresAt,

        ClassSessionDetails classSession,

        @Schema(description = "신청에 연결된 사용 수강권. WAITING과 OFFERED는 null입니다.")
        UsedPass usedPass,

        Instructor instructor
) {

    public static StudentEnrollmentDetailResponse from(StudentEnrollmentDetailView detail) {
        return new StudentEnrollmentDetailResponse(
                detail.id(),
                detail.status(),
                detail.createdAt(),
                detail.statusChangedAt(),
                detail.attendanceRecordedAt(),
                detail.waitingPosition(),
                detail.offerExpiresAt(),
                ClassSessionDetails.from(detail.classSession()),
                UsedPass.from(detail.usedPass()),
                Instructor.from(detail.instructor())
        );
    }

    @Schema(description = "신청 대상 수업 회차")
    public record ClassSessionDetails(
            Long id,
            String name,
            String description,
            LocalDateTime startAt,
            LocalDateTime endAt,
            LocalDateTime canceledAt
    ) {

        private static ClassSessionDetails from(StudentEnrollmentDetailView.ClassSessionDetails classSession) {
            return new ClassSessionDetails(
                    classSession.id(),
                    classSession.name(),
                    classSession.description(),
                    classSession.startAt(),
                    classSession.endAt(),
                    classSession.canceledAt()
            );
        }
    }

    @Schema(description = "신청에 사용된 수강권")
    public record UsedPass(
            Long id,
            String name,
            LocalDate startedAt,
            LocalDate expiresAt,
            Integer remainingCount
    ) {

        private static UsedPass from(StudentEnrollmentDetailView.UsedPass usedPass) {
            if (usedPass == null) {
                return null;
            }
            return new UsedPass(
                    usedPass.id(),
                    usedPass.name(),
                    usedPass.startedAt(),
                    usedPass.expiresAt(),
                    usedPass.remainingCount()
            );
        }
    }

    @Schema(description = "수업 담당 강사와 시설 정보")
    public record Instructor(
            Long membershipId,
            String name,
            String profileImageUrl,
            String studioName
    ) {

        private static Instructor from(StudentEnrollmentDetailView.Instructor instructor) {
            return new Instructor(
                    instructor.membershipId(),
                    instructor.name(),
                    instructor.profileImageUrl(),
                    instructor.studioName()
            );
        }
    }
}
