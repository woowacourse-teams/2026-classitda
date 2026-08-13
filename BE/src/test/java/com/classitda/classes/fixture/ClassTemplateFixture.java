package com.classitda.classes.fixture;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassTemplate;
import com.classitda.classes.presentation.dto.ClassTemplateCreateRequest;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public class ClassTemplateFixture {

    public static ClassTemplateCreateRequest 기본_수업_템플릿_생성_요청(List<Long> classTypeIds) {
        return 수업_템플릿_생성_요청(
                "저녁 요가",
                "퇴근 후 진행하는 수업",
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                classTypeIds
        );
    }

    public static ClassTemplateCreateRequest 수업_템플릿_생성_요청(
            String name,
            String description,
            Set<DayOfWeek> recurringDays,
            List<Long> classTypeIds
    ) {
        return new ClassTemplateCreateRequest(
                name,
                description,
                ClassForm.GROUP,
                60,
                LocalTime.of(20, 0),
                recurringDays,
                12,
                classTypeIds
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
}
