package com.pheeeew.sigh.domain.repository.projection;

import java.time.Instant;

public interface SighListProjection {

    Long getId();

    Instant getCreatedAt();

    String getNickname();

    String getMemo();
}
