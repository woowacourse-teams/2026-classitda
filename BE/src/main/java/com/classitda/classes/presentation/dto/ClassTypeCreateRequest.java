package com.classitda.classes.presentation.dto;

import com.classitda.classes.domain.ClassType;
import com.classitda.studio.domain.Studio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClassTypeCreateRequest(
        @NotBlank(message = "수업 종류 이름은 필수입니다.")
        @Size(min = 1, max = 50, message = "수업 종류 이름은 1자 이상 50자 이하여야 합니다.")
        String name
) {
    public static ClassTypeCreateRequest from(String name) {
        return new ClassTypeCreateRequest(name);
    }

    public ClassType toEntity(Studio studio) {
        return ClassType.builder()
                .studio(studio)
                .name(name)
                .build();
    }
}
