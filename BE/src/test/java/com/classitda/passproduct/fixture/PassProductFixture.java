package com.classitda.passproduct.fixture;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.fixture.ClassTypeFixture;
import com.classitda.passproduct.domain.PassProduct;
import com.classitda.passproduct.domain.PassProductPeriodUnit;
import com.classitda.passproduct.presentation.dto.PassProductCreateRequest;
import com.classitda.passproduct.presentation.dto.PassProductUpdateRequest;
import com.classitda.studio.domain.Studio;
import java.util.List;

public class PassProductFixture {

    public static final String 기본_이름 = "3개월 그룹 20회권";
    public static final int 기본_횟수 = 20;
    public static final int 기본_유효기간 = 3;
    public static final int 기본_홀딩_일수 = 7;
    public static final long 기본_수업_종류_아이디 = 1L;

    public static PassProductCreateRequest 기본_수강권_생성_요청() {
        return 수업_종류를_지정한_수강권_생성_요청(List.of(기본_수업_종류_아이디));
    }

    public static PassProductCreateRequest 수업_종류를_지정한_수강권_생성_요청(List<Long> classTypeIds) {
        return 수강권_생성_요청(
                기본_이름, ClassForm.GROUP, classTypeIds, 기본_횟수, 기본_유효기간, PassProductPeriodUnit.MONTH, 기본_홀딩_일수);
    }

    public static PassProductCreateRequest 수강권_생성_요청(
            String name,
            ClassForm classForm,
            List<Long> classTypeIds,
            Integer totalCount,
            Integer validPeriodAmount,
            PassProductPeriodUnit validPeriodUnit,
            Integer totalHoldDays
    ) {
        return PassProductCreateRequest.of(
                name, classForm, classTypeIds, totalCount, validPeriodAmount, validPeriodUnit, totalHoldDays);
    }

    public static PassProductUpdateRequest 기본_수강권_수정_요청() {
        return 수강권_수정_요청(
                "6개월 그룹 30회권", ClassForm.GROUP, List.of(기본_수업_종류_아이디),
                30, 6, PassProductPeriodUnit.MONTH, 14, true);
    }

    public static PassProductUpdateRequest 수강권_수정_요청(
            String name,
            ClassForm classForm,
            List<Long> classTypeIds,
            Integer totalCount,
            Integer validPeriodAmount,
            PassProductPeriodUnit validPeriodUnit,
            Integer totalHoldDays,
            Boolean active
    ) {
        return PassProductUpdateRequest.of(
                name, classForm, classTypeIds, totalCount, validPeriodAmount, validPeriodUnit, totalHoldDays, active);
    }

    public static PassProduct 기본_수강권(Studio studio) {
        return 수강권(studio, 기본_이름, ClassForm.GROUP, 기본_횟수, 기본_유효기간, PassProductPeriodUnit.MONTH, 기본_홀딩_일수);
    }

    public static PassProduct 이름이_다른_수강권(Studio studio, String name) {
        return 수강권(studio, name, ClassForm.GROUP, 기본_횟수, 기본_유효기간, PassProductPeriodUnit.MONTH, 기본_홀딩_일수);
    }

    public static PassProduct 이름과_수업_종류를_지정한_수강권(Studio studio, String name, List<ClassType> classTypes) {
        return PassProduct.builder()
                .studio(studio)
                .name(name)
                .classForm(ClassForm.GROUP)
                .classTypes(classTypes)
                .totalCount(기본_횟수)
                .validPeriodAmount(기본_유효기간)
                .validPeriodUnit(PassProductPeriodUnit.MONTH)
                .totalHoldDays(기본_홀딩_일수)
                .build();
    }

    public static PassProduct 수업_종류를_지정한_수강권(Studio studio, List<ClassType> classTypes) {
        return PassProduct.builder()
                .studio(studio)
                .name(기본_이름)
                .classForm(ClassForm.GROUP)
                .classTypes(classTypes)
                .totalCount(기본_횟수)
                .validPeriodAmount(기본_유효기간)
                .validPeriodUnit(PassProductPeriodUnit.MONTH)
                .totalHoldDays(기본_홀딩_일수)
                .build();
    }

    public static PassProduct 수강권(
            Studio studio,
            String name,
            ClassForm classForm,
            Integer totalCount,
            Integer validPeriodAmount,
            PassProductPeriodUnit validPeriodUnit,
            int totalHoldDays
    ) {
        return PassProduct.builder()
                .studio(studio)
                .name(name)
                .classForm(classForm)
                .classTypes(List.of(ClassTypeFixture.기본_수업_종류(studio)))
                .totalCount(totalCount)
                .validPeriodAmount(validPeriodAmount)
                .validPeriodUnit(validPeriodUnit)
                .totalHoldDays(totalHoldDays)
                .build();
    }
}
