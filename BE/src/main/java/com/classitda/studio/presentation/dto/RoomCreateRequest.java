package com.classitda.studio.presentation.dto;

import com.classitda.studio.domain.Room;
import com.classitda.studio.domain.Studio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoomCreateRequest(
        @NotBlank(message = "룸 이름은 필수입니다.")
        @Size(min = 1, max = 50, message = "룸 이름은 1자 이상 50자 이하여야 합니다.")
        String name
) {
    public Room toEntity(Studio studio) {
        return Room.builder()
                .studio(studio)
                .name(name)
                .build();
    }
}
