package com.classitda.classes.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.template.ClassTemplate;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record ClassTemplateResponse(
        Long id,
        String name,
        String description,
        ClassForm classForm,
        int durationMinutes,
        @JsonFormat(pattern = "HH:mm:ss")
        @Schema(type = "string", format = "time", example = "20:00:00")
        LocalTime startTime,
        List<DayOfWeek> recurringDays,
        int capacity,
        List<ClassTypeResponse> classTypes
) {

    public static ClassTemplateResponse of(
            ClassTemplate classTemplate,
            List<ClassTypeResponse> classTypes
    ) {
        List<DayOfWeek> recurringDays = classTemplate.getRecurringDays().stream()
                .sorted()
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
                classTypes
        );
    }
}
