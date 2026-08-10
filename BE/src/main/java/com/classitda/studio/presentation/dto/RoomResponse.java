package com.classitda.studio.presentation.dto;

import com.classitda.studio.domain.Room;

public record RoomResponse(
        Long id,
        String name
) {
    public static RoomResponse from(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getName()
        );
    }
}
