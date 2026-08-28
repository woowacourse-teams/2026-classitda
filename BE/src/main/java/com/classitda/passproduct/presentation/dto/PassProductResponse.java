package com.classitda.passproduct.presentation.dto;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.presentation.dto.ClassTypeResponse;
import com.classitda.passproduct.domain.PassProduct;
import com.classitda.passproduct.domain.PassProductPeriodUnit;
import java.util.List;

public record PassProductResponse(
        Long id,
        String name,
        ClassForm classForm,
        List<ClassTypeResponse> classTypes,
        Integer totalCount,
        Integer validPeriodAmount,
        PassProductPeriodUnit validPeriodUnit,
        int totalHoldDays,
        boolean active
) {
    public static PassProductResponse of(PassProduct passProduct, List<ClassType> classTypes) {
        return new PassProductResponse(
                passProduct.getId(),
                passProduct.getName(),
                passProduct.getClassForm(),
                classTypes.stream()
                        .map(classType -> ClassTypeResponse.of(classType.getId(), classType.getName()))
                        .toList(),
                passProduct.getTotalCount(),
                passProduct.getValidPeriodAmount(),
                passProduct.getValidPeriodUnit(),
                passProduct.getTotalHoldDays(),
                passProduct.isActive()
        );
    }
}
