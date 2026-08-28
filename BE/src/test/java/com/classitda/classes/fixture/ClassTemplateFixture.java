package com.classitda.classes.fixture;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.template.ClassTemplate;
import com.classitda.classes.presentation.dto.ClassTemplateCreateRequest;
import com.classitda.classes.presentation.dto.ClassTemplateUpdateRequest;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

public class ClassTemplateFixture {

    public static ClassTemplateCreateRequest 기본_수업_템플릿_생성_요청(Long classTypeId) {
        return 수업_템플릿_생성_요청(
                "저녁 요가",
                "퇴근 후 진행하는 수업",
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                classTypeId
        );
    }

    public static ClassTemplateCreateRequest 수업_템플릿_생성_요청(
            String name,
            String description,
            Set<DayOfWeek> recurringDays,
            Long classTypeId
    ) {
        return new ClassTemplateCreateRequest(
                name,
                description,
                ClassForm.GROUP,
                60,
                LocalTime.of(20, 0),
                recurringDays,
                12,
                classTypeId
        );
    }

    public static ClassTemplate 기본_수업_템플릿(Long studioId, String name, Set<DayOfWeek> recurringDays) {
        return ClassTemplate.builder()
                .studioId(studioId)
                .name(name)
                .description(null)
                .classForm(ClassForm.GROUP)
                .durationMinutes(60)
                .startTime(LocalTime.of(20, 0))
                .recurringDays(recurringDays)
                .capacity(12)
                .build();
    }

    public static ClassTemplateUpdateRequest 기본_수업_템플릿_수정_요청(Long classTypeId) {
        return 수업_템플릿_수정_요청(
                "아침 개인 필라테스",
                "개인별 자세 교정 수업",
                ClassForm.INDIVIDUAL,
                50,
                LocalTime.of(9, 30),
                Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
                1,
                classTypeId
        );
    }

    public static ClassTemplateUpdateRequest 수업_템플릿_수정_요청(
            String name,
            String description,
            ClassForm classForm,
            Integer durationMinutes,
            LocalTime startTime,
            Set<DayOfWeek> recurringDays,
            Integer capacity,
            Long classTypeId
    ) {
        return new ClassTemplateUpdateRequest(
                name,
                description,
                classForm,
                durationMinutes,
                startTime,
                recurringDays,
                capacity,
                classTypeId
        );
    }
}
