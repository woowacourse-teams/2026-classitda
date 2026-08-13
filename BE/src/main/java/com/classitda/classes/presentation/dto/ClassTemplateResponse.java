package com.classitda.classes.presentation.dto;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassTemplate;
import com.classitda.classes.domain.ClassType;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record ClassTemplateResponse(
        Long id,
        String name,
        String description,
        ClassForm classForm,
        int durationMinutes,
        LocalTime startTime,
        List<DayOfWeek> recurringDays,
        int capacity,
        List<ClassTypeResponse> classTypes
) {

    public static ClassTemplateResponse of(
            ClassTemplate classTemplate,
            List<ClassType> classTypes
    ) {
        List<DayOfWeek> recurringDays = classTemplate.getRecurringDays().stream()
                .sorted()
                .toList();
        List<ClassTypeResponse> classTypeResponses = classTypes.stream()
                .map(classType -> ClassTypeResponse.of(classType.getId(), classType.getName()))
                .toList();
        return new ClassTemplateResponse(
                classTemplate.getId(),
                classTemplate.getName(),
                classTemplate.getDescription(),
                classTemplate.getClassForm(),
                classTemplate.getDurationMinutes(),
                classTemplate.getStartTime(),
                recurringDays,
                classTemplate.getCapacity(),
                classTypeResponses
        );
    }
}
