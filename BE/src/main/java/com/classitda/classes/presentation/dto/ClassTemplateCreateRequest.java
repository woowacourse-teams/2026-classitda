package com.classitda.classes.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.classitda.classes.domain.ClassForm;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public record ClassTemplateCreateRequest(
        @NotBlank(message = "수업 템플릿 이름은 필수입니다.")
        @Size(max = 100, message = "수업 템플릿 이름은 100자 이하여야 합니다.")
        String name,

        String description,

        @NotNull(message = "수업 형태는 필수입니다.")
        ClassForm classForm,

        @NotNull(message = "진행 시간은 필수입니다.")
        @Positive(message = "진행 시간은 1분 이상이어야 합니다.")
        Integer durationMinutes,

        @NotNull(message = "시작 시간은 필수입니다.")
        @JsonFormat(pattern = "HH:mm:ss")
        @Schema(type = "string", format = "time", example = "20:00:00")
        LocalTime startTime,

        Set<@NotNull(message = "반복 요일에는 null을 포함할 수 없습니다.") DayOfWeek> recurringDays,

        @NotNull(message = "정원은 필수입니다.")
        @Positive(message = "정원은 1명 이상이어야 합니다.")
        Integer capacity,

        @NotEmpty(message = "수업 종류를 하나 이상 선택해야 합니다.")
        List<@NotNull(message = "수업 종류 ID에는 null을 포함할 수 없습니다.")
                @Positive(message = "수업 종류 ID는 양수여야 합니다.") Long> classTypeIds
) {
}
