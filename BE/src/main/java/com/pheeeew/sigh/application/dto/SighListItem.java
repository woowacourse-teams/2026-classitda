package com.pheeeew.sigh.application.dto;

import java.time.Instant;

public record SighListItem(Long id, Instant createdAt, String nickname, String memo) {

    public static SighListItem of(Long id, Instant createdAt, String nickname, String memo) {
        return new SighListItem(id, createdAt, nickname, memo);
    }
}
