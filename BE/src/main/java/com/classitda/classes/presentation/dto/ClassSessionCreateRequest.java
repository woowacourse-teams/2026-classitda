package com.classitda.classes.presentation.dto;

import com.classitda.classes.domain.ClassForm;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ClassSessionCreateRequest {

    ClassForm classForm();

    Long classTypeId();

    String className();

    Integer capacity();

    Integer durationMinutes();

    Boolean recurring();

    LocalTime startTime();

    String description();

    LocalDate classDate();

    List<DayOfWeek> recurringDays();

    LocalDate repeatStartDate();

    LocalDate repeatEndDate();
}
