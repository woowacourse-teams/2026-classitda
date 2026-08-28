package com.classitda.classes.presentation.dto;

import com.classitda.classes.domain.ClassForm;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record InstructorSessionListRequest(
        @Schema(description = "이전 응답의 nextCursor. 첫 페이지는 생략합니다.")
        String cursor,

        @Min(value = 1, message = "조회 개수는 1 이상이어야 합니다.")
        @Max(value = 100, message = "조회 개수는 100 이하여야 합니다.")
        @Schema(description = "한 번에 조회할 수업 개수", defaultValue = "10", example = "10")
        Integer size,

        @Schema(description = "수업 형태 필터")
        ClassForm classForm,

        @Positive(message = "수업 종류 ID는 양수여야 합니다.")
        @Schema(description = "수업 종류 ID 필터", example = "3")
        Long classTypeId
) {

    private static final int DEFAULT_SIZE = 10;

    public InstructorSessionListRequest {
        size = size == null ? DEFAULT_SIZE : size;
    }
}
