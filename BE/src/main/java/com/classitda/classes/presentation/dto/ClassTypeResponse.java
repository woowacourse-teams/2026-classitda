package com.classitda.classes.presentation.dto;

public record ClassTypeResponse(
        Long id,
        String name
) {
    public static ClassTypeResponse of(Long id, String name) {
        return new ClassTypeResponse(id, name);
    }
}
