package com.classitda.passproduct.presentation.dto;

import com.classitda.passproduct.domain.ClassKind;
import com.classitda.passproduct.domain.PassProductPeriodUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PassProductUpdateRequest(
        @NotBlank(message = "수강권 이름은 필수입니다.")
        @Size(max = 100, message = "수강권 이름은 100자 이하여야 합니다.")
        String name,

        @NotNull(message = "수업 형태는 필수입니다.")
        ClassKind classKind,

        @NotEmpty(message = "수업 종류를 하나 이상 지정해야 합니다.")
        List<Long> classTypeIds,

        Integer totalCount,

        Integer validPeriodAmount,

        PassProductPeriodUnit validPeriodUnit,

        @NotNull(message = "홀딩 가능 일수는 필수입니다.")
        @PositiveOrZero(message = "홀딩 가능 일수는 0일 이상이어야 합니다.")
        Integer totalHoldDays,

        @NotNull(message = "판매 여부는 필수입니다.")
        Boolean active
) {
    public static PassProductUpdateRequest of(
            String name,
            ClassKind classKind,
            List<Long> classTypeIds,
            Integer totalCount,
            Integer validPeriodAmount,
            PassProductPeriodUnit validPeriodUnit,
            Integer totalHoldDays,
            Boolean active
    ) {
        return new PassProductUpdateRequest(
                name, classKind, classTypeIds, totalCount, validPeriodAmount, validPeriodUnit, totalHoldDays, active);
    }

    public List<Long> classTypeIdsOrEmpty() {
        if (classTypeIds == null) {
            return List.of();
        }
        return classTypeIds;
    }
}
