package com.classitda.classes.presentation.dto;

import com.classitda.classes.domain.ClassForm;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ClassSessionCreateRequest(
        @Positive(message = "수업 템플릿 ID는 양수여야 합니다.")
        Long classTemplateId,

        @Schema(description = "담당 강사의 시설 소속 ID", example = "12")
        @NotNull(message = "담당 강사 소속은 필수입니다.")
        @Positive(message = "담당 강사 소속 ID는 양수여야 합니다.")
        Long instructorMembershipId,

        @NotNull(message = "수업 형태는 필수입니다.")
        ClassForm classForm,

        @NotNull(message = "수업 종류는 필수입니다.")
        @Positive(message = "수업 종류 ID는 양수여야 합니다.")
        Long classTypeId,

        @Schema(description = "수업명", example = "저녁 요가")
        @NotBlank(message = "수업 이름은 필수입니다.")
        @Size(max = 100, message = "수업 이름은 100자 이하여야 합니다.")
        String className,

        @NotNull(message = "정원은 필수입니다.")
        @Positive(message = "정원은 1명 이상이어야 합니다.")
        Integer capacity,

        @NotNull(message = "진행 시간은 필수입니다.")
        @Positive(message = "진행 시간은 1분 이상이어야 합니다.")
        Integer durationMinutes,

        @NotNull(message = "반복 여부는 필수입니다.")
        Boolean recurring,

        @NotNull(message = "수업 시작 시간은 필수입니다.")
        LocalTime startTime,

        String memo,

        LocalDate classDate,

        List<@NotNull(message = "반복 요일에는 null을 포함할 수 없습니다.") DayOfWeek> recurringDays,

        LocalDate repeatStartDate,

        LocalDate repeatEndDate
) {

    public static ClassSessionCreateRequest of(
            Long classTemplateId,
            Long instructorMembershipId,
            ClassForm classForm,
            Long classTypeId,
            String className,
            Integer capacity,
            Integer durationMinutes,
            Boolean recurring,
            LocalTime startTime,
            String memo,
            LocalDate classDate,
            List<DayOfWeek> recurringDays,
            LocalDate repeatStartDate,
            LocalDate repeatEndDate
    ) {
        return new ClassSessionCreateRequest(
                classTemplateId,
                instructorMembershipId,
                classForm,
                classTypeId,
                className,
                capacity,
                durationMinutes,
                recurring,
                startTime,
                memo,
                classDate,
                recurringDays,
                repeatStartDate,
                repeatEndDate
        );
    }
}
