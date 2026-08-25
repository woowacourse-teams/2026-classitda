package com.classitda.classes.presentation.dto;

import com.classitda.classes.domain.ClassForm;
import java.time.LocalDateTime;

public interface ClassSessionUpdateRequest {

    ClassForm classForm();

    Long classTypeId();

    String className();

    Integer capacity();

    Integer durationMinutes();

    LocalDateTime startAt();

    String description();
}
