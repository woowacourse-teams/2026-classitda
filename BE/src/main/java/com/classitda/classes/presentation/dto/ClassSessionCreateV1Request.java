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

public record ClassSessionCreateV1Request(
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

        @Schema(
                description = "수업 진행 시간(분). 1분 이상 1,440분 이하여야 합니다.",
                minimum = "1",
                maximum = "1440",
                example = "60"
        )
        @NotNull(message = "진행 시간은 필수입니다.")
        @Positive(message = "진행 시간은 1분 이상이어야 합니다.")
        Integer durationMinutes,

        @NotNull(message = "반복 여부는 필수입니다.")
        Boolean recurring,

        @NotNull(message = "수업 시작 시간은 필수입니다.")
        LocalTime startTime,

        @Schema(
                description = "회원에게 표시되는 자유 형식의 수업 안내. 준비물, 수업 장소, 입장 방법 등을 작성할 수 있습니다.",
                example = "수업은 3층 A룸에서 진행합니다. 개인 수건을 준비해 주세요."
        )
        String description,

        @Schema(
                description = "반복하지 않는 수업의 날짜입니다. recurring이 false일 때 필수입니다. 이 경우 recurringDays, repeatStartDate, repeatEndDate는 생략하거나 null로 전달해야 합니다.",
                example = "2026-08-17"
        )
        LocalDate classDate,

        @Schema(
                description = "반복 수업의 요일 목록. recurring이 true일 때 하나 이상 필요하며 null이나 중복을 포함할 수 없습니다.",
                example = "[\"MONDAY\", \"WEDNESDAY\"]"
        )
        List<@NotNull(message = "반복 요일에는 null을 포함할 수 없습니다.") DayOfWeek> recurringDays,

        @Schema(
                description = "반복 기간의 시작일. recurring이 true일 때 필수이며 이 날짜를 포함합니다.",
                example = "2026-08-17"
        )
        LocalDate repeatStartDate,

        @Schema(
                description = "반복 기간의 종료일. recurring이 true일 때 필수이며 시작일보다 빠를 수 없고 이 날짜를 포함합니다.",
                example = "2026-08-31"
        )
        LocalDate repeatEndDate
) implements ClassSessionCreateRequest {
}
