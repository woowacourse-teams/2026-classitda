package com.classitda.classes.presentation.dto;

import com.classitda.classes.domain.ClassForm;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record ClassSessionUpdateRequest(
        @Schema(description = "수업 형태. 생략하면 기존 값을 유지합니다.")
        ClassForm classForm,

        @Schema(
                description = "같은 시설에 속한 수업 종류 ID. 생략하면 기존 값을 유지합니다.",
                example = "1"
        )
        @Positive(message = "수업 종류 ID는 양수여야 합니다.")
        Long classTypeId,

        @Schema(description = "수업명. 생략하면 기존 값을 유지합니다.", example = "저녁 요가")
        @Pattern(regexp = "(?s).*\\S.*", message = "수업 이름은 공백일 수 없습니다.")
        @Size(max = 100, message = "수업 이름은 100자 이하여야 합니다.")
        String className,

        @Schema(description = "정원. 생략하면 기존 값을 유지합니다.", example = "12")
        @Positive(message = "정원은 1명 이상이어야 합니다.")
        Integer capacity,

        @Schema(
                description = "수업 진행 시간(분). 생략하면 기존 값을 유지하며, 1분 이상 1,440분 이하여야 합니다.",
                minimum = "1",
                maximum = "1440",
                example = "60"
        )
        @Positive(message = "진행 시간은 1분 이상이어야 합니다.")
        @Max(value = 1440, message = "진행 시간은 1,440분 이하여야 합니다.")
        Integer durationMinutes,

        @Schema(
                description = "수업 시작 일시. 생략하면 기존 값을 유지합니다.",
                example = "2026-08-30T20:00:00"
        )
        LocalDateTime startAt,

        @Schema(
                description = "회원에게 표시되는 자유 형식의 수업 안내. "
                        + "생략하거나 null이면 기존 값을 유지하고 빈 문자열이면 안내를 비웁니다.",
                example = "수업은 3층 A룸에서 진행합니다. 개인 수건을 준비해 주세요."
        )
        String description
) {

    public static ClassSessionUpdateRequest of(
            ClassForm classForm,
            Long classTypeId,
            String className,
            Integer capacity,
            Integer durationMinutes,
            LocalDateTime startAt,
            String description
    ) {
        return new ClassSessionUpdateRequest(
                classForm,
                classTypeId,
                className,
                capacity,
                durationMinutes,
                startAt,
                description
        );
    }
}
