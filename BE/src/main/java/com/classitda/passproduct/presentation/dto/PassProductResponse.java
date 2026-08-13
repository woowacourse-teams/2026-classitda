package com.classitda.passproduct.presentation.dto;

import com.classitda.passproduct.domain.ClassKind;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.presentation.dto.ClassTypeResponse;
import com.classitda.passproduct.domain.PassProductPeriodUnit;
import com.classitda.passproduct.domain.PassProduct;
import java.util.List;

public record PassProductResponse(
        Long id,
        String name,
        ClassKind classKind,
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
                passProduct.getClassKind(),
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
