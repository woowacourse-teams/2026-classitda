package com.classitda.passproduct.presentation.dto;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassType;
import com.classitda.passproduct.domain.PassProduct;
import com.classitda.passproduct.domain.PassProductPeriodUnit;
import com.classitda.studio.domain.Studio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PassProductCreateRequest(
        @NotBlank(message = "수강권 이름은 필수입니다.")
        @Size(max = 100, message = "수강권 이름은 100자 이하여야 합니다.")
        String name,

        @NotNull(message = "수업 형태는 필수입니다.")
        ClassForm classForm,

        @NotEmpty(message = "수업 종류를 하나 이상 지정해야 합니다.")
        List<Long> classTypeIds,

        Integer totalCount,

        Integer validPeriodAmount,

        PassProductPeriodUnit validPeriodUnit,

        @NotNull(message = "홀딩 가능 일수는 필수입니다.")
        @PositiveOrZero(message = "홀딩 가능 일수는 0일 이상이어야 합니다.")
        Integer totalHoldDays
) {
    public static PassProductCreateRequest of(
            String name,
            ClassForm classForm,
            List<Long> classTypeIds,
            Integer totalCount,
            Integer validPeriodAmount,
            PassProductPeriodUnit validPeriodUnit,
            Integer totalHoldDays
    ) {
        return new PassProductCreateRequest(
                name, classForm, classTypeIds, totalCount, validPeriodAmount, validPeriodUnit, totalHoldDays);
    }

    public PassProduct toEntity(Studio studio, List<ClassType> classTypes) {
        return PassProduct.builder()
                .studio(studio)
                .name(name)
                .classForm(classForm)
                .classTypes(classTypes)
                .totalCount(totalCount)
                .validPeriodAmount(validPeriodAmount)
                .validPeriodUnit(validPeriodUnit)
                .totalHoldDays(totalHoldDays)
                .build();
    }

    public List<Long> classTypeIdsOrEmpty() {
        if (classTypeIds == null) {
            return List.of();
        }
        return classTypeIds;
    }
}
